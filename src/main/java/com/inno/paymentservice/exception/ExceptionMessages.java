package com.inno.paymentservice.exception;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ExceptionMessages {
  // Order exceptions
  public static final String INVALID_ORDER_STATUS = "Order status must be PENDING, order id = %d";

  // External service exceptions
  public static final String SERVICE_UNAVAILABLE =
      "Payment service is temporarily unavailable. Please try again later";

  // Auth exceptions
  public static final String ACCESS_DENIED = "Access denied: insufficient permissions";
}
