package com.inno.paymentservice.controller;

import static com.inno.paymentservice.util.ConfigConstants.USER_ID_HEADER;

import com.inno.paymentservice.dto.request.PaymentFilterDto;
import com.inno.paymentservice.dto.request.TotalSumFilterRequestDto;
import com.inno.paymentservice.dto.response.PaymentResponseDto;
import com.inno.paymentservice.dto.response.TotalSumResponseDto;
import com.inno.paymentservice.model.PaymentEvent;
import com.inno.paymentservice.producer.PaymentProducer;
import com.inno.paymentservice.service.PaymentService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/v1/payments")
public class PaymentController {

  private final PaymentService paymentService;
  private final PaymentProducer paymentProducer;

  @PostMapping("/{orderId}")
  public ResponseEntity<PaymentResponseDto> create(
      @PathVariable Long orderId, @RequestHeader(USER_ID_HEADER) Long userId) {
    PaymentResponseDto result = paymentService.create(orderId, userId);
    paymentProducer.sendEvent(PaymentEvent.from(result));
    return ResponseEntity.ok(result);
  }

  @GetMapping("/filter")
  @PreAuthorize(
      "hasRole('ADMIN') or #filter.userId() == null or #filter.userId() == authentication.principal")
  public ResponseEntity<List<PaymentResponseDto>> getAll(
      PaymentFilterDto filter, @RequestHeader(USER_ID_HEADER) Long userId) {
    return ResponseEntity.ok(paymentService.getAll(filter, userId));
  }

  @GetMapping("/total-sum")
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<TotalSumResponseDto> getTotalSumByUser(
      @Valid TotalSumFilterRequestDto filter, @AuthenticationPrincipal Long userId) {
    return ResponseEntity.ok(paymentService.getTotalSumByUser(userId, filter));
  }

  @GetMapping("/total-sum/all")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<TotalSumResponseDto> getTotalSum(@Valid TotalSumFilterRequestDto filter) {
    return ResponseEntity.ok(paymentService.getTotalSum(filter));
  }
}
