package com.inno.paymentservice.dto.request.externalpayment;

import java.util.UUID;
import lombok.Builder;

@Builder
public record ExternalPaymentServiceRequestDto(
    String jsonrpc, String method, ExternalServiceParamsRequestDto params, String id) {
  public static ExternalPaymentServiceRequestDto of(String apiKey, int min, int max) {
    return new ExternalPaymentServiceRequestDto(
        "2.0",
        "generateIntegers",
        new ExternalServiceParamsRequestDto(apiKey, 1, min, max),
        UUID.randomUUID().toString());
  }
}
