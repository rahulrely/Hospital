
package com.example.payment.controller;

import com.example.payment.service.StripeWebhookService;
import com.google.gson.JsonSyntaxException;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/api/stripe")
public class StripeWebhookController {

    private final StripeWebhookService webhookService;
    private final String webhookSecret;

    public StripeWebhookController(
            StripeWebhookService webhookService,
            @Value("${stripe.webhook-secret}") String webhookSecret) {
        this.webhookService = webhookService;
        this.webhookSecret = webhookSecret;
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signature) {

        log.info("Stripe webhook received by backend");

        final Event event;

        try {
            event = Webhook.constructEvent(
                    payload, signature, webhookSecret);
        } catch (SignatureVerificationException ex) {
            log.error("Stripe signature verification failed", ex);
            return ResponseEntity.badRequest()
                    .body("Invalid Stripe signature");
        } catch (JsonSyntaxException ex) {
            log.error("Stripe webhook JSON parsing failed", ex);
            return ResponseEntity.badRequest()
                    .body("Invalid Stripe webhook payload");
        }

        String type = event.getType();

        log.info(
                "Verified Stripe event: {}, API version: {}",
                type,
                event.getApiVersion()
        );

        if (!"checkout.session.completed".equals(type)
                && !"checkout.session.async_payment_succeeded".equals(type)) {
            log.info("Ignoring unsupported event: {}", type);
            return ResponseEntity.ok("Event ignored");
        }

        try {
            var deserializer = event.getDataObjectDeserializer();

            StripeObject object = deserializer.getObject()
                    .orElseGet(() -> {
                        log.warn(
                                "Normal deserialization failed for event {}. "
                                        + "Attempting fallback.",
                                event.getId()
                        );

                        try {
                            return deserializer.deserializeUnsafe();
                        } catch (Exception ex) {
                            log.error(
                                    "Fallback deserialization failed. "
                                            + "Event ID={}, API version={}",
                                    event.getId(),
                                    event.getApiVersion(),
                                    ex
                            );
                            return null;
                        }
                    });

            if (!(object instanceof Session session)) {
                log.error(
                        "Unable to deserialize Checkout Session. "
                                + "Event ID={}, API version={}",
                        event.getId(),
                        event.getApiVersion()
                );

                return ResponseEntity.badRequest()
                        .body("Unable to deserialize Checkout Session");
            }

            log.info(
                    "Checkout Session received: {}, payment status: {}",
                    session.getId(),
                    session.getPaymentStatus()
            );

            if (!"paid".equalsIgnoreCase(session.getPaymentStatus())) {
                log.info(
                        "Checkout Session is not paid yet: {}",
                        session.getId()
                );
                return ResponseEntity.ok("Payment not completed");
            }

            webhookService.handleCheckoutPaid(event.getId(), session);

            log.info(
                    "Stripe webhook processed successfully: {}",
                    event.getId()
            );

            return ResponseEntity.ok(
                    "Webhook received and processed");

        } catch (Exception ex) {
            log.error(
                    "Failed to process Stripe webhook event: {}",
                    event.getId(),
                    ex
            );

            return ResponseEntity.internalServerError()
                    .body("Webhook processing failed");
        }
    }
}
