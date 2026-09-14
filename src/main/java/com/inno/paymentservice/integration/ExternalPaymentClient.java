package com.inno.paymentservice.integration;

import com.inno.paymentservice.configuration.FeignConfig;
import com.inno.paymentservice.dto.request.externalpayment.ExternalPaymentServiceRequestDto;
import com.inno.paymentservice.dto.response.externalpayment.ExternalPaymentResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
    name = "external-payment-service",
    url = "${application.integration.external-payment-service.url}",
    configuration = FeignConfig.class)
public interface ExternalPaymentClient {

  @PostMapping("/json-rpc/4/invoke")
  ExternalPaymentResponseDto executePayment(@RequestBody ExternalPaymentServiceRequestDto request);
}
