package com.inno.paymentservice.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.kafka.topic.payment")
public record KafkaTopicProperties(String name, int partitions, int replicas, String retentionMs) {}
