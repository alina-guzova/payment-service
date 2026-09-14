package com.inno.paymentservice.service.impl;

import static com.inno.paymentservice.exception.BaseAppExceptionBuilder.buildBadRequestException;
import static com.inno.paymentservice.exception.ExceptionMessages.INVALID_ORDER_STATUS;

import com.inno.paymentservice.configuration.ExternalPaymentProperties;
import com.inno.paymentservice.dto.request.PaymentFilterDto;
import com.inno.paymentservice.dto.request.TotalSumFilterRequestDto;
import com.inno.paymentservice.dto.request.externalpayment.ExternalPaymentServiceRequestDto;
import com.inno.paymentservice.dto.response.PaymentResponseDto;
import com.inno.paymentservice.dto.response.TotalSumResponseDto;
import com.inno.paymentservice.dto.response.externalpayment.ExternalPaymentResponseDto;
import com.inno.paymentservice.dto.response.order.OrderStatus;
import com.inno.paymentservice.dto.response.order.OrderWithUserInfoResponseDto;
import com.inno.paymentservice.entity.Payment;
import com.inno.paymentservice.entity.PaymentStatus;
import com.inno.paymentservice.integration.ExternalPaymentClient;
import com.inno.paymentservice.integration.OrderServiceClient;
import com.inno.paymentservice.mapper.PaymentMapper;
import com.inno.paymentservice.repository.PaymentRepository;
import com.inno.paymentservice.security.PaymentSecurityResolver;
import com.inno.paymentservice.security.PaymentSecurityValidator;
import com.inno.paymentservice.service.PaymentService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class PaymentServiceImpl implements PaymentService {

  private final ExternalPaymentProperties externalPaymentProperties;
  private final PaymentRepository paymentRepository;
  private final OrderServiceClient orderServiceClient;
  private final ExternalPaymentClient externalPaymentClient;
  private final PaymentMapper paymentMapper;
  private final PaymentSecurityValidator securityValidator;
  private final PaymentSecurityResolver paymentSecurityResolver;

  @Override
  @Transactional
  public PaymentResponseDto create(Long orderId, Long currentUserId) {
    OrderWithUserInfoResponseDto orderInfo = orderServiceClient.getOrderInfoById(orderId);

    securityValidator.validateOrderOwner(orderInfo.userInfo().id(), currentUserId);
    validateOrderStatus(orderInfo);

    ExternalPaymentResponseDto response =
        externalPaymentClient.executePayment(
            ExternalPaymentServiceRequestDto.of(externalPaymentProperties.apiKey(), 1, 100));
    PaymentStatus status = resolvePaymentStatus(response.extractFirstValue());

    Payment payment = paymentMapper.toPayment(orderInfo, status);

    Payment savedPayment = paymentRepository.save(payment);
    log.info("Payment result | status={}, orderId={}", status, orderId);

    return paymentMapper.toPaymentResponseDto(savedPayment);
  }

  @Override
  @Transactional(readOnly = true)
  public List<PaymentResponseDto> getAll(PaymentFilterDto filter, Long currentUserId) {
    PaymentFilterDto resolvedFilter = paymentSecurityResolver.resolveFilter(filter, currentUserId);

    List<Payment> payments =
        paymentRepository.findPaymentsByCriteria(
            resolvedFilter.orderId(), resolvedFilter.userId(), resolvedFilter.status());

    return payments.stream().map(paymentMapper::toPaymentResponseDto).toList();
  }

  @Override
  @Transactional(readOnly = true)
  public TotalSumResponseDto getTotalSumByUser(Long userId, TotalSumFilterRequestDto filter) {
    TotalSumResponseDto result = paymentRepository.getTotalSumByUser(userId, filter);

    return resolveTotalSum(result);
  }

  @Override
  @Transactional(readOnly = true)
  public TotalSumResponseDto getTotalSum(TotalSumFilterRequestDto filter) {
    TotalSumResponseDto result = paymentRepository.getTotalSum(filter);

    return resolveTotalSum(result);
  }

  private void validateOrderStatus(OrderWithUserInfoResponseDto orderInfo) {
    Long orderId = orderInfo.order().id();
    OrderStatus status = orderInfo.order().status();

    if (status != OrderStatus.PENDING) {
      log.warn("Payment failed: Invalid order status | orderId={}, status={}", orderId, status);
      throw buildBadRequestException(INVALID_ORDER_STATUS, orderId);
    }
  }

  private PaymentStatus resolvePaymentStatus(int paymentResult) {
    return paymentResult % 2 == 0 ? PaymentStatus.SUCCESS : PaymentStatus.FAILED;
  }

  private TotalSumResponseDto resolveTotalSum(TotalSumResponseDto total) {
    return total != null ? total : new TotalSumResponseDto(0.0);
  }
}
