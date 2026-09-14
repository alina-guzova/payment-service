package com.inno.paymentservice.dto.response.externalpayment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ExternalPaymentRandomResultDto(ExternalPaymentDataDto random) {}
