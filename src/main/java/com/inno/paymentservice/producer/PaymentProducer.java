package com.inno.paymentservice.producer;

import com.inno.paymentservice.configuration.KafkaTopicProperties;
import com.inno.paymentservice.model.PaymentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentProducer {

  private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;
  private final KafkaTopicProperties topicProperties;

  public void sendEvent(PaymentEvent event) {
    kafkaTemplate
        .send(topicProperties.name(), event.paymentId(), event)
        .whenComplete(
            (result, ex) -> {
              if (ex == null) {
                log.info(
                    "Sent message=[{}] with offset=[{}]",
                    event,
                    result.getRecordMetadata().offset());
              } else {
                log.error("Unable to send message=[{}] due to : {}", event, ex.getMessage());
              }
            });
  }
}
