package com.inno.paymentservice.dto.response.order;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderWithUserInfoResponseDto(
    UserInfoResponseDto userInfo, OrderInfoResponseDto order) {}
