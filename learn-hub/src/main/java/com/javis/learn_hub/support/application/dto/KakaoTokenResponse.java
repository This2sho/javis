package com.javis.learn_hub.support.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KakaoTokenResponse(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("expires_in") String expiresIn,
        @JsonProperty("refresh_token") String refreshToken
) {

}

