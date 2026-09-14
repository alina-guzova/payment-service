package com.inno.paymentservice.dto.request;

import com.inno.paymentservice.entity.PaymentStatus;

public record PaymentFilterDto(Long orderId, Long userId, PaymentStatus status) {}
