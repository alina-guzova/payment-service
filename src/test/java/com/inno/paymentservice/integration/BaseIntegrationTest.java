package com.inno.paymentservice.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import java.time.Duration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public abstract class BaseIntegrationTest {

  static final MongoDBContainer mongo =
      new MongoDBContainer(DockerImageName.parse("mongo:7.0"))
          .withStartupTimeout(Duration.ofSeconds(120));

  static final KafkaContainer kafka =
      new KafkaContainer(DockerImageName.parse("apache/kafka:3.7.0"))
          .withStartupTimeout(Duration.ofSeconds(120));

  protected static final WireMockServer wireMockServer =
      new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());

  @BeforeAll
  static void startContainers() {
    mongo.start();
    kafka.start();
    wireMockServer.start();
  }

  @AfterAll
  static void stopContainers() {
    wireMockServer.stop();
    mongo.stop();
    kafka.stop();
  }

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    String mongoUri = mongo.getReplicaSetUrl();
    registry.add("spring.data.mongodb.uri", () -> mongoUri);
    registry.add("spring.liquibase.url", () -> mongoUri);

    registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);

    registry.add("application.integration.order-service.url", wireMockServer::baseUrl);
    registry.add("application.integration.external-payment-service.url", wireMockServer::baseUrl);
  }
}
