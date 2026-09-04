package com.inno.paymentservice.dto.response.externalpayment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ExternalPaymentDataDto(List<Integer> data) {}
