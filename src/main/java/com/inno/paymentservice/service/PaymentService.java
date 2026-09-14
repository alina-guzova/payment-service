package com.inno.paymentservice.service;

import com.inno.paymentservice.dto.request.PaymentFilterDto;
import com.inno.paymentservice.dto.request.TotalSumFilterRequestDto;
import com.inno.paymentservice.dto.response.PaymentResponseDto;
import com.inno.paymentservice.dto.response.TotalSumResponseDto;
import java.util.List;

public interface PaymentService {

  PaymentResponseDto create(Long orderId, Long userId);

  List<PaymentResponseDto> getAll(PaymentFilterDto filter, Long userId);

  TotalSumResponseDto getTotalSumByUser(Long userId, TotalSumFilterRequestDto filter);

  TotalSumResponseDto getTotalSum(TotalSumFilterRequestDto filter);
}
