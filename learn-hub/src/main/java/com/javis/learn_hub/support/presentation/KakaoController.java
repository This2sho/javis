package com.javis.learn_hub.support.presentation;

import com.javis.learn_hub.support.domain.Provider;
import com.javis.learn_hub.support.infrastructure.KakaoOauthProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

@RequestMapping("/kakao")
@RequiredArgsConstructor
@Controller
public class KakaoController {

    private final KakaoOauthProvider kakaoOauthProvider;

    @GetMapping("/sign-in")
    public RedirectView login() {
        return new RedirectView(kakaoOauthProvider.getAuthUrl());
    }

    // 카카오에서 인증 후 돌아오는 콜백
    @GetMapping("/callback")
    public String callback(
            @RequestParam String code,
            Model model
    ) {
        String accessToken = kakaoOauthProvider.handleAuthorizationCallback(code);
        model.addAttribute("accessToken", accessToken);
        model.addAttribute("provider", Provider.KAKAO.name());
        return "login-success";
    }
}
