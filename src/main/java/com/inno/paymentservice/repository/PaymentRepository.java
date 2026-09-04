package com.inno.paymentservice.repository;

import com.inno.paymentservice.entity.Payment;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository
    extends MongoRepository<Payment, String>, PaymentRepositoryCustom {
  List<Payment> findByOrderId(Long orderId);
}
