package com.inno.paymentservice.configuration;

import com.inno.paymentservice.model.PaymentEvent;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

@Configuration
@EnableConfigurationProperties(KafkaTopicProperties.class)
@RequiredArgsConstructor
public class KafkaProducerConfig {

  @Value("${spring.kafka.bootstrap-servers}")
  private String bootstrapServers;

  private final KafkaTopicProperties topicProperties;

  @Bean
  public ProducerFactory<String, PaymentEvent> producerFactory() {
    Map<String, Object> configProps = new HashMap<>();
    configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
    configProps.put(ProducerConfig.RETRIES_CONFIG, Integer.MAX_VALUE);
    configProps.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 1000);
    configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
    configProps.put(ProducerConfig.ACKS_CONFIG, "all");
    return new DefaultKafkaProducerFactory<>(configProps);
  }

  @Bean
  public NewTopic paymentEventsTopic() {
    return TopicBuilder.name(topicProperties.name())
        .partitions(topicProperties.partitions())
        .replicas(topicProperties.replicas())
        .config(TopicConfig.RETENTION_MS_CONFIG, topicProperties.retentionMs())
        .build();
  }

  @Bean
  public KafkaTemplate<String, PaymentEvent> kafkaTemplate() {
    return new KafkaTemplate<>(producerFactory());
  }
}
