package com.inno.paymentservice.exception;

import lombok.experimental.UtilityClass;
import org.springframework.http.HttpStatus;

@UtilityClass
public class BaseAppExceptionBuilder {

  public static BaseAppException buildNotFoundException(String messagePattern, Object... args) {
    return new BaseAppException(messagePattern.formatted(args), HttpStatus.NOT_FOUND);
  }

  public static BaseAppException buildConflictException(String messagePattern, Object... args) {
    return new BaseAppException(messagePattern.formatted(args), HttpStatus.CONFLICT);
  }

  public static BaseAppException buildBadRequestException(String messagePattern, Object... args) {
    return new BaseAppException(messagePattern.formatted(args), HttpStatus.BAD_REQUEST);
  }

  public static BaseAppException buildServiceUnavailableException(
      String messagePattern, Object... args) {
    return new BaseAppException(messagePattern.formatted(args), HttpStatus.SERVICE_UNAVAILABLE);
  }

  public static BaseAppException buildForbiddenException(String messagePattern, Object... args) {
    return new BaseAppException(messagePattern.formatted(args), HttpStatus.FORBIDDEN);
  }
}
