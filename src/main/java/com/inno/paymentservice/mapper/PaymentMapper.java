package com.inno.paymentservice.mapper;

import com.inno.paymentservice.dto.response.PaymentResponseDto;
import com.inno.paymentservice.dto.response.order.OrderWithUserInfoResponseDto;
import com.inno.paymentservice.entity.Payment;
import com.inno.paymentservice.entity.PaymentStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PaymentMapper {

  @Mapping(target = "orderId", source = "dto.order.id")
  @Mapping(target = "userId", source = "dto.userInfo.id")
  @Mapping(target = "paymentAmount", source = "dto.order.totalPrice")
  Payment toPayment(OrderWithUserInfoResponseDto dto, PaymentStatus status);

  PaymentResponseDto toPaymentResponseDto(Payment payment);
}
