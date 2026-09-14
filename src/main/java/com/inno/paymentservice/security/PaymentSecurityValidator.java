package com.inno.paymentservice.security;

import static com.inno.paymentservice.exception.BaseAppExceptionBuilder.buildForbiddenException;
import static com.inno.paymentservice.exception.ExceptionMessages.ACCESS_DENIED;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PaymentSecurityValidator {

  public void validateOrderOwner(Long orderOwnerId, Long currentUserId) {
    if (!orderOwnerId.equals(currentUserId)) {
      log.warn(
          "Access denied: User tried to access another user's order | currentUserId={}, orderOwnerId={}",
          currentUserId,
          orderOwnerId);
      throw buildForbiddenException(ACCESS_DENIED);
    }
  }
}
