package com.inno.paymentservice.dto.response;

import com.inno.paymentservice.entity.PaymentStatus;
import java.time.Instant;
import lombok.Builder;

@Builder
public record PaymentResponseDto(
    String id,
    Long orderId,
    Long userId,
    PaymentStatus status,
    Double paymentAmount,
    Instant timestamp) {}
