package com.inno.paymentservice.util;

import com.inno.paymentservice.dto.response.PaymentResponseDto;
import com.inno.paymentservice.dto.response.externalpayment.ExternalPaymentDataDto;
import com.inno.paymentservice.dto.response.externalpayment.ExternalPaymentRandomResultDto;
import com.inno.paymentservice.dto.response.externalpayment.ExternalPaymentResponseDto;
import com.inno.paymentservice.dto.response.order.OrderInfoResponseDto;
import com.inno.paymentservice.dto.response.order.OrderStatus;
import com.inno.paymentservice.dto.response.order.OrderWithUserInfoResponseDto;
import com.inno.paymentservice.dto.response.order.UserInfoResponseDto;
import com.inno.paymentservice.entity.Payment;
import com.inno.paymentservice.entity.PaymentStatus;
import java.util.List;

public final class PaymentTestData {

  // --- API Paths ---
  public static final String PAYMENTS_PATH_WITH_ORDER_ID = "/api/v1/payments/{orderId}";
  public static final String ORDERS_API_PATH = "/api/v1/orders/";
  public static final String PAYMENTS_PATH_FILTER = "/api/v1/payments/filter";
  public static final String PAYMENTS_PATH_TOTAL_SUM = "/api/v1/payments/total-sum";
  public static final String PAYMENTS_PATH_TOTAL_SUM_ALL = "/api/v1/payments/total-sum/all";

  // --- Headers ---
  public static final String USER_ID_HEADER = "X-User-Id";

  // --- Roles ---
  public static final String USER = "ROLE_USER";
  public static final String ADMIN = "ROLE_ADMIN";

  // --- Test Identifiers & Numeric Values ---
  public static final Long DEFAULT_ORDER_ID = 1L;
  public static final Long SECOND_ORDER_ID = 2L;
  public static final Long THIRD_ORDER_ID = 3L;
  public static final Long FOURTH_ORDER_ID = 4L;
  public static final Long DEFAULT_USER_ID = 1L;
  public static final Long SECOND_USER_ID = 2L;
  public static final Long CURRENT_USER_ID = 999L;
  public static final Double DEFAULT_TOTAL_PRICE = 150.0;
  public static final Integer SUCCESS_PAYMENT_RESULT = 4;
  public static final Integer FAILED_PAYMENT_RESULT = 5;
  public static final double DEFAULT_TOTAL_SUM = 300.0;
  public static final double ZERO_TOTAL_SUM = 0.0;

  // --- Text Attributes ---
  public static final String DEFAULT_PAYMENT_ID = "payment_mongo_id_123";
  public static final String DEFAULT_API_KEY = "test-api-key";
  public static final String DEFAULT_EXTERNAL_ID = "ext_id_789";
  public static final String STATUS_FAILED = "FAILED";
  public static final String STATUS_SUCCESS = "SUCCESS";

  private PaymentTestData() {}

  public static OrderWithUserInfoResponseDto buildOrderResponseDto(OrderStatus status) {
    return new OrderWithUserInfoResponseDto(
        new UserInfoResponseDto(DEFAULT_USER_ID),
        new OrderInfoResponseDto(DEFAULT_ORDER_ID, status, DEFAULT_TOTAL_PRICE));
  }

  public static ExternalPaymentResponseDto buildExternalPaymentResponse(int resultValue) {
    return ExternalPaymentResponseDto.builder()
        .id(DEFAULT_EXTERNAL_ID)
        .result(
            new ExternalPaymentRandomResultDto(new ExternalPaymentDataDto(List.of(resultValue))))
        .build();
  }

  public static Payment buildPaymentEntity(PaymentStatus status) {
    return Payment.builder()
        .id(DEFAULT_PAYMENT_ID)
        .orderId(DEFAULT_ORDER_ID)
        .userId(DEFAULT_USER_ID)
        .status(status)
        .paymentAmount(DEFAULT_TOTAL_PRICE)
        .build();
  }

  public static PaymentResponseDto buildPaymentResponseDto(PaymentStatus status) {
    return PaymentResponseDto.builder()
        .id(DEFAULT_PAYMENT_ID)
        .orderId(DEFAULT_ORDER_ID)
        .userId(DEFAULT_USER_ID)
        .status(status)
        .paymentAmount(DEFAULT_TOTAL_PRICE)
        .build();
  }
}
