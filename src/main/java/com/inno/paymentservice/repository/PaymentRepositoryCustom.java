package com.inno.paymentservice.repository;

import com.inno.paymentservice.dto.request.TotalSumFilterRequestDto;
import com.inno.paymentservice.dto.response.TotalSumResponseDto;
import com.inno.paymentservice.entity.Payment;
import com.inno.paymentservice.entity.PaymentStatus;
import java.util.List;

public interface PaymentRepositoryCustom {
  List<Payment> findPaymentsByCriteria(Long orderId, Long userId, PaymentStatus status);

  TotalSumResponseDto getTotalSumByUser(Long userId, TotalSumFilterRequestDto filter);

  TotalSumResponseDto getTotalSum(TotalSumFilterRequestDto filter);
}
