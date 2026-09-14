package com.inno.paymentservice.model;

import com.inno.paymentservice.dto.response.PaymentResponseDto;
import com.inno.paymentservice.entity.PaymentStatus;

public record PaymentEvent(
    String paymentId, Long orderId, PaymentStatus status, Double paymentAmount) {
  public static PaymentEvent from(PaymentResponseDto paymentInfo) {
    return new PaymentEvent(
        paymentInfo.id(), paymentInfo.orderId(), paymentInfo.status(), paymentInfo.paymentAmount());
  }
}
