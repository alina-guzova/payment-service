package com.inno.paymentservice.dto.response.externalpayment;

import static com.inno.paymentservice.exception.BaseAppExceptionBuilder.buildServiceUnavailableException;
import static com.inno.paymentservice.exception.ExceptionMessages.SERVICE_UNAVAILABLE;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public record ExternalPaymentResponseDto(ExternalPaymentRandomResultDto result, String id) {
  public int extractFirstValue() {
    if (result != null
        && result.random() != null
        && result.random().data() != null
        && !result.random().data().isEmpty()) {
      return result.random().data().getFirst();
    } else throw buildServiceUnavailableException(SERVICE_UNAVAILABLE);
  }
}
