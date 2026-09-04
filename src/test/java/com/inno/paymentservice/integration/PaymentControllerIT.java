package com.inno.paymentservice.integration;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.inno.paymentservice.util.PaymentTestData.ADMIN;
import static com.inno.paymentservice.util.PaymentTestData.CURRENT_USER_ID;
import static com.inno.paymentservice.util.PaymentTestData.DEFAULT_ORDER_ID;
import static com.inno.paymentservice.util.PaymentTestData.DEFAULT_TOTAL_PRICE;
import static com.inno.paymentservice.util.PaymentTestData.DEFAULT_USER_ID;
import static com.inno.paymentservice.util.PaymentTestData.FAILED_PAYMENT_RESULT;
import static com.inno.paymentservice.util.PaymentTestData.FOURTH_ORDER_ID;
import static com.inno.paymentservice.util.PaymentTestData.ORDERS_API_PATH;
import static com.inno.paymentservice.util.PaymentTestData.PAYMENTS_PATH_FILTER;
import static com.inno.paymentservice.util.PaymentTestData.PAYMENTS_PATH_TOTAL_SUM;
import static com.inno.paymentservice.util.PaymentTestData.PAYMENTS_PATH_TOTAL_SUM_ALL;
import static com.inno.paymentservice.util.PaymentTestData.PAYMENTS_PATH_WITH_ORDER_ID;
import static com.inno.paymentservice.util.PaymentTestData.SECOND_ORDER_ID;
import static com.inno.paymentservice.util.PaymentTestData.SECOND_USER_ID;
import static com.inno.paymentservice.util.PaymentTestData.STATUS_FAILED;
import static com.inno.paymentservice.util.PaymentTestData.STATUS_SUCCESS;
import static com.inno.paymentservice.util.PaymentTestData.SUCCESS_PAYMENT_RESULT;
import static com.inno.paymentservice.util.PaymentTestData.THIRD_ORDER_ID;
import static com.inno.paymentservice.util.PaymentTestData.USER;
import static com.inno.paymentservice.util.PaymentTestData.USER_ID_HEADER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.securityContext;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inno.paymentservice.dto.request.PaymentFilterDto;
import com.inno.paymentservice.dto.response.order.OrderStatus;
import com.inno.paymentservice.dto.response.order.OrderWithUserInfoResponseDto;
import com.inno.paymentservice.entity.Payment;
import com.inno.paymentservice.entity.PaymentStatus;
import com.inno.paymentservice.producer.PaymentProducer;
import com.inno.paymentservice.repository.PaymentRepository;
import com.inno.paymentservice.util.PaymentTestData;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@ActiveProfiles("test")
class PaymentControllerIT extends BaseIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private PaymentRepository paymentRepository;

  @MockitoBean private KafkaTemplate<String, Object> kafkaTemplate;

  @MockitoBean private PaymentProducer paymentProducer;

  @Value("${application.integration.order-service.url}")
  private String orderUrl;

  @Value("${application.integration.external-payment-service.url}")
  private String paymentUrl;

  @BeforeEach
  void setUp() throws Exception {
    paymentRepository.deleteAll();
  }

  private RequestPostProcessor userAuth(Long userId, String role) {
    return request -> {
      SecurityContext context = SecurityContextHolder.createEmptyContext();
      UsernamePasswordAuthenticationToken auth =
          new UsernamePasswordAuthenticationToken(
              userId, null, List.of(new SimpleGrantedAuthority(role)));
      context.setAuthentication(auth);
      return securityContext(context).postProcessRequest(request);
    };
  }

  @Nested
  class CreateTests {

    @Test
    void shouldCreatePaymentSuccessfully() throws Exception {
      // given
      Long orderId = PaymentTestData.DEFAULT_ORDER_ID;
      Long userId = PaymentTestData.DEFAULT_USER_ID;

      OrderWithUserInfoResponseDto orderResponse =
          PaymentTestData.buildOrderResponseDto(OrderStatus.PENDING);

      wireMockServer.stubFor(
          get(urlEqualTo(ORDERS_API_PATH + orderId))
              .willReturn(
                  aResponse()
                      .withStatus(200)
                      .withHeader("Content-Type", "application/json")
                      .withBody(objectMapper.writeValueAsString(orderResponse))));

      wireMockServer.stubFor(
          com.github.tomakehurst.wiremock.client.WireMock.post(urlEqualTo("/json-rpc/4/invoke"))
              .willReturn(
                  aResponse()
                      .withStatus(200)
                      .withHeader("Content-Type", "application/json")
                      .withBody(
                          objectMapper.writeValueAsString(
                              PaymentTestData.buildExternalPaymentResponse(
                                  SUCCESS_PAYMENT_RESULT)))));

      // when & then
      mockMvc
          .perform(
              post(PAYMENTS_PATH_WITH_ORDER_ID, orderId)
                  .with(userAuth(userId, USER))
                  .header(USER_ID_HEADER, userId)
                  .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.orderId", is(orderId.intValue())))
          .andExpect(jsonPath("$.userId", is(userId.intValue())))
          .andExpect(jsonPath("$.status", is(STATUS_SUCCESS)))
          .andExpect(jsonPath("$.paymentAmount", is(DEFAULT_TOTAL_PRICE)));

      assertThat(paymentRepository.count()).isEqualTo(1);

      Payment savedPayment = paymentRepository.findAll().getFirst();
      assertThat(savedPayment.getOrderId()).isEqualTo(orderId);
      assertThat(savedPayment.getUserId()).isEqualTo(userId);
      assertThat(savedPayment.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
      assertThat(savedPayment.getPaymentAmount()).isEqualTo(DEFAULT_TOTAL_PRICE);

      verify(paymentProducer)
          .sendEvent(
              argThat(
                  event -> {
                    assertThat(event.orderId()).isEqualTo(orderId);
                    assertThat(event.status()).isEqualTo(PaymentStatus.SUCCESS);
                    return true;
                  }));
    }

    @Test
    void shouldFailWhenExternalPaymentFails() throws Exception {
      // given
      Long orderId = PaymentTestData.DEFAULT_ORDER_ID;
      Long userId = PaymentTestData.DEFAULT_USER_ID;

      OrderWithUserInfoResponseDto orderResponse =
          PaymentTestData.buildOrderResponseDto(OrderStatus.PENDING);

      wireMockServer.stubFor(
          get(urlEqualTo(ORDERS_API_PATH + orderId))
              .willReturn(
                  aResponse()
                      .withStatus(200)
                      .withHeader("Content-Type", "application/json")
                      .withBody(objectMapper.writeValueAsString(orderResponse))));

      wireMockServer.stubFor(
          com.github.tomakehurst.wiremock.client.WireMock.post(urlEqualTo("/json-rpc/4/invoke"))
              .willReturn(
                  aResponse()
                      .withStatus(200)
                      .withHeader("Content-Type", "application/json")
                      .withBody(
                          objectMapper.writeValueAsString(
                              PaymentTestData.buildExternalPaymentResponse(
                                  FAILED_PAYMENT_RESULT)))));

      // when & then
      mockMvc
          .perform(
              post(PAYMENTS_PATH_WITH_ORDER_ID, orderId)
                  .with(userAuth(userId, USER))
                  .header(USER_ID_HEADER, userId)
                  .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.status", is(STATUS_FAILED)));

      Payment savedPayment = paymentRepository.findAll().getFirst();
      assertThat(savedPayment.getStatus()).isEqualTo(PaymentStatus.FAILED);

      verify(paymentProducer)
          .sendEvent(
              argThat(
                  event -> {
                    assertThat(event.orderId()).isEqualTo(orderId);
                    assertThat(event.status()).isEqualTo(PaymentStatus.FAILED);
                    return true;
                  }));
    }

    @Test
    void shouldFailWhenUserIsAdmin() throws Exception {
      Long orderId = PaymentTestData.DEFAULT_ORDER_ID;
      Long adminUserId = 999L;

      OrderWithUserInfoResponseDto orderResponse =
          PaymentTestData.buildOrderResponseDto(OrderStatus.PENDING);

      wireMockServer.stubFor(
          get(urlEqualTo(ORDERS_API_PATH + orderId))
              .willReturn(
                  aResponse()
                      .withStatus(200)
                      .withHeader("Content-Type", "application/json")
                      .withBody(objectMapper.writeValueAsString(orderResponse))));

      mockMvc
          .perform(
              post(PAYMENTS_PATH_WITH_ORDER_ID, orderId)
                  .with(userAuth(adminUserId, ADMIN))
                  .header(USER_ID_HEADER, adminUserId)
                  .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isForbidden());

      assertThat(paymentRepository.count()).isZero();
    }
  }

  @Nested
  class GetAllTests {

    @Test
    void shouldGetFilteredPaymentsAsAdmin() throws Exception {
      Long targetUserId = DEFAULT_USER_ID;
      Long otherUserId = SECOND_USER_ID;
      Long adminId = CURRENT_USER_ID;

      paymentRepository.saveAll(
          List.of(
              Payment.builder()
                  .orderId(DEFAULT_ORDER_ID)
                  .userId(targetUserId)
                  .status(PaymentStatus.SUCCESS)
                  .paymentAmount(DEFAULT_TOTAL_PRICE)
                  .build(),
              Payment.builder()
                  .orderId(SECOND_ORDER_ID)
                  .userId(targetUserId)
                  .status(PaymentStatus.FAILED)
                  .paymentAmount(DEFAULT_TOTAL_PRICE)
                  .build(),
              Payment.builder()
                  .orderId(THIRD_ORDER_ID)
                  .userId(otherUserId)
                  .status(PaymentStatus.FAILED)
                  .paymentAmount(DEFAULT_TOTAL_PRICE)
                  .build()));

      mockMvc
          .perform(
              org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get(
                      PAYMENTS_PATH_FILTER)
                  .queryParam("userId", targetUserId.toString())
                  .queryParam("status", STATUS_SUCCESS)
                  .with(userAuth(adminId, ADMIN))
                  .header(USER_ID_HEADER, adminId))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.size()", is(1)))
          .andExpect(jsonPath("$[0].userId", is(targetUserId.intValue())))
          .andExpect(jsonPath("$[0].status", is(STATUS_SUCCESS)));
    }

    @Test
    void shouldThrowForbiddenWhenRegularUserFiltersByAnotherUserId() throws Exception {
      Long targetUserId = SECOND_USER_ID;
      Long currentUserId = DEFAULT_USER_ID;
      PaymentFilterDto filter = new PaymentFilterDto(null, targetUserId, PaymentStatus.SUCCESS);

      mockMvc
          .perform(
              org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get(
                      PAYMENTS_PATH_FILTER)
                  .queryParam("userId", targetUserId.toString())
                  .queryParam("status", STATUS_SUCCESS)
                  .with(userAuth(currentUserId, USER))
                  .header(USER_ID_HEADER, currentUserId))
          .andExpect(status().isForbidden());
    }
  }

  @Nested
  class GetTotalSumByUserTests {

    @Test
    void shouldGetTotalSumForUserWhenPaymentsExist() throws Exception {
      Long userId = DEFAULT_USER_ID;

      paymentRepository.saveAll(
          List.of(
              Payment.builder()
                  .orderId(DEFAULT_ORDER_ID)
                  .userId(userId)
                  .status(PaymentStatus.FAILED)
                  .paymentAmount(DEFAULT_TOTAL_PRICE)
                  .build(),
              Payment.builder()
                  .orderId(DEFAULT_ORDER_ID)
                  .userId(userId)
                  .status(PaymentStatus.SUCCESS)
                  .paymentAmount(DEFAULT_TOTAL_PRICE)
                  .build()));

      mockMvc
          .perform(
              org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get(
                      PAYMENTS_PATH_TOTAL_SUM)
                  .with(userAuth(userId, USER))
                  .header(USER_ID_HEADER, userId))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.total", is(DEFAULT_TOTAL_PRICE)));
    }

    @Test
    void shouldThrowForbiddenWhenAdminTriesToGetTotalSum() throws Exception {
      Long adminId = CURRENT_USER_ID;

      mockMvc
          .perform(
              org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get(
                      PAYMENTS_PATH_TOTAL_SUM)
                  .with(userAuth(adminId, ADMIN))
                  .header(USER_ID_HEADER, adminId))
          .andExpect(status().isForbidden());
    }
  }

  @Nested
  class GetTotalSumTests {

    @Test
    void shouldGetTotalSumOfAllSuccessfulPaymentsForAdmin() throws Exception {
      Long adminId = CURRENT_USER_ID;
      Long firstUserId = DEFAULT_USER_ID;
      Long secondUserId = SECOND_USER_ID;

      paymentRepository.saveAll(
          List.of(
              Payment.builder()
                  .orderId(DEFAULT_ORDER_ID)
                  .userId(firstUserId)
                  .status(PaymentStatus.FAILED)
                  .paymentAmount(DEFAULT_TOTAL_PRICE)
                  .build(),
              Payment.builder()
                  .orderId(SECOND_ORDER_ID)
                  .userId(firstUserId)
                  .status(PaymentStatus.SUCCESS)
                  .paymentAmount(DEFAULT_TOTAL_PRICE)
                  .build(),
              Payment.builder()
                  .orderId(THIRD_ORDER_ID)
                  .userId(secondUserId)
                  .status(PaymentStatus.FAILED)
                  .paymentAmount(DEFAULT_TOTAL_PRICE)
                  .build(),
              Payment.builder()
                  .orderId(FOURTH_ORDER_ID)
                  .userId(secondUserId)
                  .status(PaymentStatus.SUCCESS)
                  .paymentAmount(DEFAULT_TOTAL_PRICE)
                  .build()));

      mockMvc
          .perform(
              org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get(
                      PAYMENTS_PATH_TOTAL_SUM_ALL)
                  .with(userAuth(adminId, ADMIN))
                  .header(USER_ID_HEADER, adminId))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.total", is(DEFAULT_TOTAL_PRICE * 2)));
    }

    @Test
    void shouldThrowForbiddenWhenRegularUserTriesToGetTotalSumForAll() throws Exception {
      Long userId = DEFAULT_USER_ID;

      mockMvc
          .perform(
              org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get(
                      PAYMENTS_PATH_TOTAL_SUM_ALL)
                  .with(userAuth(userId, USER))
                  .header(USER_ID_HEADER, userId))
          .andExpect(status().isForbidden());
    }
  }
}
