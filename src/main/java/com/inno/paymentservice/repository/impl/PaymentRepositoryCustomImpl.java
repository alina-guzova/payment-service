package com.inno.paymentservice.repository.impl;

import com.inno.paymentservice.dto.request.TotalSumFilterRequestDto;
import com.inno.paymentservice.dto.response.TotalSumResponseDto;
import com.inno.paymentservice.entity.Payment;
import com.inno.paymentservice.entity.PaymentStatus;
import com.inno.paymentservice.repository.PaymentRepositoryCustom;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PaymentRepositoryCustomImpl implements PaymentRepositoryCustom {

  private final MongoTemplate mongoTemplate;

  @Override
  public List<Payment> findPaymentsByCriteria(Long orderId, Long userId, PaymentStatus status) {
    Criteria criteria = new Criteria();

    if (orderId != null) {
      criteria.and("order_id").is(orderId);
    }
    if (userId != null) {
      criteria.and("user_id").is(userId);
    }
    if (status != null) {
      criteria.and("status").is(status);
    }

    Query query = new Query(criteria);
    return mongoTemplate.find(query, Payment.class);
  }

  @Override
  public TotalSumResponseDto getTotalSumByUser(Long userId, TotalSumFilterRequestDto filter) {
    Criteria criteria =
        Criteria.where("user_id").is(userId).and("status").is(PaymentStatus.SUCCESS);

    applyDateFilter(criteria, filter);

    Aggregation aggregation =
        Aggregation.newAggregation(
            Aggregation.match(criteria), Aggregation.group().sum("payment_amount").as("total"));

    AggregationResults<TotalSumResponseDto> results =
        mongoTemplate.aggregate(aggregation, Payment.class, TotalSumResponseDto.class);

    return results.getUniqueMappedResult();
  }

  @Override
  public TotalSumResponseDto getTotalSum(TotalSumFilterRequestDto filter) {
    Criteria criteria = Criteria.where("status").is(PaymentStatus.SUCCESS);

    applyDateFilter(criteria, filter);

    Aggregation aggregation =
        Aggregation.newAggregation(
            Aggregation.match(criteria), Aggregation.group().sum("payment_amount").as("total"));

    AggregationResults<TotalSumResponseDto> results =
        mongoTemplate.aggregate(aggregation, Payment.class, TotalSumResponseDto.class);

    return results.getUniqueMappedResult();
  }

  private void applyDateFilter(Criteria criteria, TotalSumFilterRequestDto filter) {
    if (filter == null) {
      return;
    }
    if (filter.minDate() != null && filter.maxDate() != null) {
      criteria
          .and("timestamp")
          .gte(filter.minDate().atStartOfDay())
          .lte(filter.maxDate().atTime(LocalTime.MAX));
    } else if (filter.minDate() != null) {
      criteria.and("timestamp").gte(filter.minDate().atStartOfDay());
    } else if (filter.maxDate() != null) {
      criteria.and("timestamp").lte(filter.maxDate().atTime(LocalTime.MAX));
    }
  }
}
