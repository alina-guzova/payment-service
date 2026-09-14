package com.inno.paymentservice.entity;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "payments")
public class Payment {

  @Id private String id;

  @Field(value = "order_id")
  private Long orderId;

  @Field(value = "user_id")
  private Long userId;

  @Field(value = "status")
  private PaymentStatus status;

  @Field(value = "payment_amount")
  private Double paymentAmount;

  @Field(value = "timestamp")
  @CreatedDate
  private Instant timestamp;
}
