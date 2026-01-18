package com.javis.learn_hub.support.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javis.learn_hub.support.application.dto.KakaoProfileResponse;
import com.javis.learn_hub.support.application.dto.KakaoTokenResponse;
import com.javis.learn_hub.support.domain.OauthProvider;
import com.javis.learn_hub.support.domain.UserProfile;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@RequiredArgsConstructor
@Component
public class KakaoOauthProvider implements OauthProvider {

    private static final String PROVIDER_NAME = "kakao";

    @Value("${kakao.client-id}")
    private String clientId;

    @Value("${kakao.client-secret}")
    private String clientSecret;

    @Value("${kakao.kauth-host}")
    private String kauthHost;

    @Value("${kakao.kapi-host}")
    private String kapiHost;

    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public String getAuthUrl() {
        return UriComponentsBuilder.fromHttpUrl(kauthHost + "/oauth/authorize").queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri).queryParam("response_type", "code").build().toUriString();
    }

    public String handleAuthorizationCallback(String code) {
        try {
            String params = String.format(
                    "grant_type=authorization_code&client_id=%s&client_secret=%s&code=%s&redirect_uri=%s", clientId,
                    clientSecret, code, redirectUri);
            String response = call("POST", kauthHost + "/oauth/token", params, "");
            KakaoTokenResponse token = objectMapper.readValue(response, KakaoTokenResponse.class);
            Objects.requireNonNull(token.accessToken());
            return token.accessToken();
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException("인증 불가");
        }
    }

    private String call(String method, String url, String body, String accessToken) throws Exception {
        RestClient.RequestBodySpec req = restClient.method(HttpMethod.valueOf(method)).uri(url)
                .headers(h -> h.setBearerAuth(accessToken));
        if (body != null) {
            req.contentType(MediaType.APPLICATION_FORM_URLENCODED).body(body);
        }
        return req.retrieve().body(String.class);
    }

    @Override
    public UserProfile getUserProfile(String accessToken) {
        try {
            String response = call("GET", kapiHost + "/v2/user/me", null, accessToken);
            return objectMapper.readValue(response, KakaoProfileResponse.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean is(String name) {
        return PROVIDER_NAME.equals(name);
    }
}
