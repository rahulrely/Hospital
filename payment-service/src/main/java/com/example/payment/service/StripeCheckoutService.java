
package com.example.payment.service;

import com.example.payment.entity.Payment;
import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Locale;
import java.util.Objects;

@Service
public class StripeCheckoutService {

    private final StripeClient stripeClient;
    private final String currency;
    private final String successUrl;
    private final String cancelUrl;

    public StripeCheckoutService(
            StripeClient stripeClient,
            @Value("${stripe.currency:inr}") String currency,
            @Value("${app.frontend.success-url}") String successUrl,
            @Value("${app.frontend.cancel-url}") String cancelUrl) {

        this.stripeClient = Objects.requireNonNull(stripeClient);
        this.currency = currency.toLowerCase(Locale.ROOT);
        this.successUrl = successUrl;
        this.cancelUrl = cancelUrl;
    }

    public Session createCheckoutSession(Payment payment)
            throws StripeException {

        Objects.requireNonNull(payment, "Payment must not be null");

        if (payment.getId() == null) {
            throw new IllegalArgumentException(
                    "Payment must be persisted before creating a Checkout Session");
        }

        if (payment.getAmount() == null
                || payment.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Payment amount must be greater than zero");
        }

        if (payment.getAppointmentId() == null
                || payment.getPatientId() == null
                || payment.getType() == null) {
            throw new IllegalArgumentException(
                    "Payment appointment, patient, and type are required");
        }

        int fractionDigits = Currency.getInstance(
                currency.toUpperCase(Locale.ROOT)
        ).getDefaultFractionDigits();

        if (fractionDigits < 0) {
            throw new IllegalArgumentException(
                    "Unsupported currency: " + currency);
        }

        long amountInMinorUnits = payment.getAmount()
                .movePointRight(fractionDigits)
                .longValueExact();

        if (amountInMinorUnits <= 0) {
            throw new IllegalArgumentException(
                    "Payment amount is too small for currency " + currency);
        }

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setClientReferenceId(
                        payment.getAppointmentId().toString())
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)

                // Metadata used by StripeWebhookService
                .putMetadata("paymentId", payment.getId().toString())
                .putMetadata(
                        "appointmentId",
                        payment.getAppointmentId().toString())
                .putMetadata(
                        "patientId",
                        payment.getPatientId().toString())
                .putMetadata("paymentType", payment.getType().name())

                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData
                                                .builder()
                                                .setCurrency(currency)
                                                .setUnitAmount(amountInMinorUnits)
                                                .setProductData(
                                                        SessionCreateParams.LineItem
                                                                .PriceData.ProductData
                                                                .builder()
                                                                .setName(productName(payment))
                                                                .build())
                                                .build())
                                .build())
                .build();

        return stripeClient.v1()
                .checkout()
                .sessions()
                .create(params);
    }

    private String productName(Payment payment) {
        return switch (payment.getType()) {
            case CONSULTATION_FEE ->
                    "Dermatology consultation fee";
            case MEDICINE ->
                    "Dermatology clinic medicine bill";
            case LAB_TEST ->
                    "Dermatology clinic lab test";
            case PROCEDURE ->
                    "Dermatology clinic procedure";
        };
    }
}
