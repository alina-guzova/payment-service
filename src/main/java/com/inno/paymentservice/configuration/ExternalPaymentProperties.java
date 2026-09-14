package com.inno.paymentservice.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "application.integration.external-payment-service")
public record ExternalPaymentProperties(String apiKey) {}
