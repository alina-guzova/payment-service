package com.inno.paymentservice.integration;

import com.inno.paymentservice.configuration.FeignConfig;
import com.inno.paymentservice.dto.response.order.OrderWithUserInfoResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
    name = "order-service",
    url = "${application.integration.order-service.url}",
    configuration = FeignConfig.class)
public interface OrderServiceClient {

  @GetMapping("api/v1/orders/{orderId}")
  OrderWithUserInfoResponseDto getOrderInfoById(@PathVariable("orderId") Long orderId);
}
