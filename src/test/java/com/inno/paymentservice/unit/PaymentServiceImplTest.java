package com.inno.paymentservice.unit;

import static com.inno.paymentservice.exception.ExceptionMessages.ACCESS_DENIED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.inno.paymentservice.configuration.ExternalPaymentProperties;
import com.inno.paymentservice.dto.request.PaymentFilterDto;
import com.inno.paymentservice.dto.request.TotalSumFilterRequestDto;
import com.inno.paymentservice.dto.response.PaymentResponseDto;
import com.inno.paymentservice.dto.response.TotalSumResponseDto;
import com.inno.paymentservice.dto.response.externalpayment.ExternalPaymentResponseDto;
import com.inno.paymentservice.dto.response.order.OrderStatus;
import com.inno.paymentservice.dto.response.order.OrderWithUserInfoResponseDto;
import com.inno.paymentservice.entity.Payment;
import com.inno.paymentservice.entity.PaymentStatus;
import com.inno.paymentservice.exception.BaseAppException;
import com.inno.paymentservice.integration.ExternalPaymentClient;
import com.inno.paymentservice.integration.OrderServiceClient;
import com.inno.paymentservice.mapper.PaymentMapper;
import com.inno.paymentservice.repository.PaymentRepository;
import com.inno.paymentservice.security.PaymentSecurityResolver;
import com.inno.paymentservice.security.PaymentSecurityValidator;
import com.inno.paymentservice.service.impl.PaymentServiceImpl;
import com.inno.paymentservice.util.PaymentTestData;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

  @Mock private ExternalPaymentProperties externalPaymentProperties;
  @Mock private PaymentRepository paymentRepository;
  @Mock private OrderServiceClient orderServiceClient;
  @Mock private ExternalPaymentClient externalPaymentClient;
  @Mock private PaymentMapper paymentMapper;
  @Mock private PaymentSecurityValidator securityValidator;
  @Mock private PaymentSecurityResolver paymentSecurityResolver;

  @InjectMocks private PaymentServiceImpl paymentService;

  @Nested
  class CreateTests {

    @Test
    void whenCreatePaymentWithEvenResult_thenPaymentIsSavedAndSuccessReturned() {
      // given
      Long orderId = PaymentTestData.DEFAULT_ORDER_ID;
      Long userId = PaymentTestData.DEFAULT_USER_ID;
      OrderWithUserInfoResponseDto orderInfo =
          PaymentTestData.buildOrderResponseDto(OrderStatus.PENDING);
      ExternalPaymentResponseDto externalResponse =
          PaymentTestData.buildExternalPaymentResponse(PaymentTestData.SUCCESS_PAYMENT_RESULT);
      Payment mappedPayment = PaymentTestData.buildPaymentEntity(PaymentStatus.SUCCESS);
      Payment savedPayment = PaymentTestData.buildPaymentEntity(PaymentStatus.SUCCESS);
      PaymentResponseDto expectedResponse =
          PaymentTestData.buildPaymentResponseDto(PaymentStatus.SUCCESS);

      when(orderServiceClient.getOrderInfoById(orderId)).thenReturn(orderInfo);
      when(externalPaymentProperties.apiKey()).thenReturn(PaymentTestData.DEFAULT_API_KEY);
      when(externalPaymentClient.executePayment(any())).thenReturn(externalResponse);
      when(paymentMapper.toPayment(orderInfo, PaymentStatus.SUCCESS)).thenReturn(mappedPayment);
      when(paymentRepository.save(mappedPayment)).thenReturn(savedPayment);
      when(paymentMapper.toPaymentResponseDto(savedPayment)).thenReturn(expectedResponse);

      // when
      PaymentResponseDto actualResponse = paymentService.create(orderId, userId);

      // then
      assertThat(actualResponse).isNotNull().isEqualTo(expectedResponse);

      verify(orderServiceClient).getOrderInfoById(orderId);
      verify(securityValidator).validateOrderOwner(userId, userId);
      verify(externalPaymentClient).executePayment(any());
      verify(paymentMapper).toPayment(orderInfo, PaymentStatus.SUCCESS);
      verify(paymentRepository).save(mappedPayment);
      verify(paymentMapper).toPaymentResponseDto(savedPayment);
    }

    @Test
    void whenCreatePaymentByDifferentUser_thenThrowsForbiddenException() {
      // given
      Long orderId = PaymentTestData.DEFAULT_ORDER_ID;
      Long currentUserId = PaymentTestData.CURRENT_USER_ID;
      OrderWithUserInfoResponseDto orderInfo =
          PaymentTestData.buildOrderResponseDto(OrderStatus.PENDING);

      when(orderServiceClient.getOrderInfoById(orderId)).thenReturn(orderInfo);
      doThrow(new BaseAppException(ACCESS_DENIED, HttpStatus.FORBIDDEN))
          .when(securityValidator)
          .validateOrderOwner(orderInfo.userInfo().id(), currentUserId);

      // when & then
      BaseAppException exception =
          assertThrows(BaseAppException.class, () -> paymentService.create(orderId, currentUserId));

      assertThat(exception.getStatus()).isEqualTo(HttpStatus.FORBIDDEN);

      verify(orderServiceClient).getOrderInfoById(orderId);
      verify(securityValidator).validateOrderOwner(orderInfo.userInfo().id(), currentUserId);
      verifyNoInteractions(externalPaymentClient, paymentRepository, paymentMapper);
    }

    @Test
    void whenCreatePaymentWithInvalidOrderStatus_thenThrowsBadRequestException() {
      // given
      Long orderId = PaymentTestData.DEFAULT_ORDER_ID;
      Long userId = PaymentTestData.DEFAULT_USER_ID;
      OrderWithUserInfoResponseDto orderInfo =
          PaymentTestData.buildOrderResponseDto(OrderStatus.DONE);

      when(orderServiceClient.getOrderInfoById(orderId)).thenReturn(orderInfo);

      // when & then
      BaseAppException exception =
          assertThrows(BaseAppException.class, () -> paymentService.create(orderId, userId));

      assertThat(exception.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);

      verify(orderServiceClient).getOrderInfoById(orderId);
      verify(securityValidator).validateOrderOwner(orderInfo.userInfo().id(), userId);
      verifyNoInteractions(externalPaymentClient, paymentRepository, paymentMapper);
    }

    @Test
    void whenCreatePaymentWithOddResult_thenPaymentIsSavedAndFailedReturned() {
      // given
      Long orderId = PaymentTestData.DEFAULT_ORDER_ID;
      Long userId = PaymentTestData.DEFAULT_USER_ID;
      OrderWithUserInfoResponseDto orderInfo =
          PaymentTestData.buildOrderResponseDto(OrderStatus.PENDING);
      ExternalPaymentResponseDto externalResponse =
          PaymentTestData.buildExternalPaymentResponse(PaymentTestData.FAILED_PAYMENT_RESULT);
      Payment mappedPayment = PaymentTestData.buildPaymentEntity(PaymentStatus.FAILED);
      Payment savedPayment = PaymentTestData.buildPaymentEntity(PaymentStatus.FAILED);
      PaymentResponseDto expectedResponse =
          PaymentTestData.buildPaymentResponseDto(PaymentStatus.FAILED);

      when(orderServiceClient.getOrderInfoById(orderId)).thenReturn(orderInfo);
      when(externalPaymentProperties.apiKey()).thenReturn(PaymentTestData.DEFAULT_API_KEY);
      when(externalPaymentClient.executePayment(any())).thenReturn(externalResponse);
      when(paymentMapper.toPayment(orderInfo, PaymentStatus.FAILED)).thenReturn(mappedPayment);
      when(paymentRepository.save(mappedPayment)).thenReturn(savedPayment);
      when(paymentMapper.toPaymentResponseDto(savedPayment)).thenReturn(expectedResponse);

      // when
      PaymentResponseDto actualResponse = paymentService.create(orderId, userId);

      // then
      assertThat(actualResponse).isNotNull().isEqualTo(expectedResponse);

      verify(orderServiceClient).getOrderInfoById(orderId);
      verify(securityValidator).validateOrderOwner(orderInfo.userInfo().id(), userId);
      verify(externalPaymentClient).executePayment(any());
      verify(paymentMapper).toPayment(orderInfo, PaymentStatus.FAILED);
      verify(paymentRepository).save(mappedPayment);
      verify(paymentMapper).toPaymentResponseDto(savedPayment);
    }
  }

  @Nested
  class GetAllTests {

    @Test
    void whenGetByOrderIdAsAdmin_thenReturnsFilteredPayments() {
      // given
      Long currentUserId = PaymentTestData.DEFAULT_USER_ID;
      PaymentFilterDto filter =
          new PaymentFilterDto(
              PaymentTestData.DEFAULT_ORDER_ID,
              PaymentTestData.CURRENT_USER_ID,
              PaymentStatus.SUCCESS);
      List<Payment> payments = List.of(PaymentTestData.buildPaymentEntity(PaymentStatus.SUCCESS));
      PaymentResponseDto expectedResponse =
          PaymentTestData.buildPaymentResponseDto(PaymentStatus.SUCCESS);

      when(paymentSecurityResolver.resolveFilter(filter, currentUserId)).thenReturn(filter);
      when(paymentRepository.findPaymentsByCriteria(
              filter.orderId(), filter.userId(), filter.status()))
          .thenReturn(payments);
      when(paymentMapper.toPaymentResponseDto(payments.getFirst())).thenReturn(expectedResponse);

      // when
      List<PaymentResponseDto> actualResponses = paymentService.getAll(filter, currentUserId);

      // then
      assertThat(actualResponses).isNotNull().hasSize(1).containsExactly(expectedResponse);

      verify(paymentSecurityResolver).resolveFilter(filter, currentUserId);
      verify(paymentRepository)
          .findPaymentsByCriteria(filter.orderId(), filter.userId(), filter.status());
      verify(paymentMapper).toPaymentResponseDto(payments.getFirst());
    }
  }

  @Nested
  class GetTotalSumTests {

    @Test
    void whenGetTotalSumByUser_thenReturnsSum() {
      // given
      Long userId = PaymentTestData.DEFAULT_USER_ID;
      TotalSumFilterRequestDto filter = null;
      TotalSumResponseDto expectedResponse =
          new TotalSumResponseDto(PaymentTestData.DEFAULT_TOTAL_SUM);

      when(paymentRepository.getTotalSumByUser(userId, filter)).thenReturn(expectedResponse);

      // when
      TotalSumResponseDto actualResponse = paymentService.getTotalSumByUser(userId, filter);

      // then
      assertThat(actualResponse).isNotNull().isEqualTo(expectedResponse);

      verify(paymentRepository).getTotalSumByUser(userId, filter);
    }

    @Test
    void whenGetTotalSumByUserAndResultIsNull_thenReturnsZeroSum() {
      // given
      Long userId = PaymentTestData.DEFAULT_USER_ID;
      TotalSumFilterRequestDto filter = null;
      TotalSumResponseDto expectedResponse =
          new TotalSumResponseDto(PaymentTestData.ZERO_TOTAL_SUM);

      when(paymentRepository.getTotalSumByUser(userId, filter)).thenReturn(null);

      // when
      TotalSumResponseDto actualResponse = paymentService.getTotalSumByUser(userId, filter);

      // then
      assertThat(actualResponse).isNotNull().isEqualTo(expectedResponse);

      verify(paymentRepository).getTotalSumByUser(userId, filter);
    }
  }

  @Nested
  class GetTotalSumGlobalTests {

    @Test
    void whenGetTotalSum_thenReturnsSum() {
      // given
      TotalSumResponseDto expectedResponse =
          new TotalSumResponseDto(PaymentTestData.DEFAULT_TOTAL_SUM);
      TotalSumFilterRequestDto filter = null;
      when(paymentRepository.getTotalSum(filter)).thenReturn(expectedResponse);

      // when
      TotalSumResponseDto actualResponse = paymentService.getTotalSum(filter);

      // then
      assertThat(actualResponse).isNotNull().isEqualTo(expectedResponse);

      verify(paymentRepository).getTotalSum(filter);
    }

    @Test
    void whenGetTotalSumAndResultIsNull_thenReturnsZeroSum() {
      // given
      TotalSumResponseDto expectedResponse =
          new TotalSumResponseDto(PaymentTestData.ZERO_TOTAL_SUM);
      TotalSumFilterRequestDto filter = null;
      when(paymentRepository.getTotalSum(filter)).thenReturn(null);

      // when
      TotalSumResponseDto actualResponse = paymentService.getTotalSum(filter);

      // then
      assertThat(actualResponse).isNotNull().isEqualTo(expectedResponse);

      verify(paymentRepository).getTotalSum(filter);
    }
  }
}
