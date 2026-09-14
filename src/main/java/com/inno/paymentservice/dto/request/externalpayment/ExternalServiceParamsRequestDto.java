package com.inno.paymentservice.dto.request.externalpayment;

import lombok.Builder;

@Builder
public record ExternalServiceParamsRequestDto(String apiKey, int n, int min, int max) {}
