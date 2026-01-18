package com.javis.learn_hub.support.application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.javis.learn_hub.member.domain.repository.MemberRepository;
import com.javis.learn_hub.support.application.dto.KakaoProfileResponse;
import com.javis.learn_hub.support.builder.MemberBuilder;
import com.javis.learn_hub.support.domain.OauthProvider;
import com.javis.learn_hub.support.domain.OauthProviders;
import com.javis.learn_hub.support.domain.UserProfile;
import com.javis.learn_hub.support.infrastructure.KakaoOauthProvider;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class LoginServiceTest {

    @MockitoBean
    private MemberRepository memberRepository;

    @MockitoBean
    private OauthProviders oauthProviders;

    @MockitoBean
    private KakaoOauthProvider kakaoOauthProvider;

    @Autowired
    private LoginService loginService;

    @DisplayName("[첫 로그인] 해당하는 OAuth accessToken을 가져와 가입하고 서비스 자체 토큰을 반환한다.")
    @Test
    void testSignIn() {
        //given
        OauthProvider oauthProvider = kakaoOauthProvider;
        given(oauthProviders.mapping(any()))
                .willReturn(oauthProvider);

        UserProfile userProfile = new KakaoProfileResponse(1L);
        given(kakaoOauthProvider.getUserProfile(any()))
                .willReturn(userProfile);

        //when
        loginService.signIn("kakao", "accessToken");

        //then
        verify(memberRepository).save(any());
    }

    @DisplayName("[재 로그인] 해당하는 OAuth accessToken을 가져와 자체 토큰을 반환한다.")
    @Test
    void testSignIn2() {
        //given
        OauthProvider oauthProvider = kakaoOauthProvider;
        given(oauthProviders.mapping(any()))
                .willReturn(oauthProvider);

        UserProfile userProfile = new KakaoProfileResponse(1L);
        given(kakaoOauthProvider.getUserProfile(any()))
                .willReturn(userProfile);

        given(memberRepository.findByProviderAndSocialId(any(), any()))
                .willReturn(Optional.of(MemberBuilder.builder().build()));
        //when
        loginService.signIn("kakao", "accessToken");

        //then
        verify(memberRepository, never()).save(any());
    }
}
