package com.javis.learn_hub.support.presentation;

import static org.springframework.http.HttpHeaders.SET_COOKIE;

import com.javis.learn_hub.support.application.LoginService;
import com.javis.learn_hub.support.application.dto.LoginRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
public class LoginController {

    private static final int COOKIE_AGE_SECONDS = 60 * 60 * 24 * 30;
    private final LoginService loginService;

    @PostMapping("/login/{provider}")
    public ResponseEntity<Void> signIn(
            @PathVariable String provider,
            @RequestBody LoginRequest request,
            HttpServletResponse response
    ) {
        String accessToken = loginService.signIn(provider, request.accessToken());
        ResponseCookie cookie = ResponseCookie.from("access-token", accessToken)
                .maxAge(COOKIE_AGE_SECONDS)
                .sameSite("None")
                .secure(true)
                .httpOnly(true)
                .path("/")
                .build();
        response.addHeader(SET_COOKIE, cookie.toString());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
