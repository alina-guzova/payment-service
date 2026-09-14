package com.inno.paymentservice.security;

import static com.inno.paymentservice.exception.BaseAppExceptionBuilder.buildForbiddenException;
import static com.inno.paymentservice.exception.ExceptionMessages.ACCESS_DENIED;

import com.inno.paymentservice.dto.request.PaymentFilterDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentSecurityResolver {

  public PaymentFilterDto resolveFilter(PaymentFilterDto filter, Long currentUserId) {
    if (hasAdminRole()) {
      return filter;
    }

    if (filter.userId() != null && !filter.userId().equals(currentUserId)) {
      log.warn(
          "Access denied: Regular user tried filtering by another user ID | currentUserId={}, requestedUserId={}",
          currentUserId,
          filter.userId());
      throw buildForbiddenException(ACCESS_DENIED);
    }

    return new PaymentFilterDto(filter.orderId(), currentUserId, filter.status());
  }

  private boolean hasAdminRole() {
    return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
        .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
  }
}
