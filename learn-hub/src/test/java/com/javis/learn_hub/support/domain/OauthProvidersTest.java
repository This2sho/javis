package com.javis.learn_hub.support.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.javis.learn_hub.support.infrastructure.KakaoOauthProvider;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OauthProvidersTest {

    private OauthProviders oauthProviders = new OauthProviders(
            List.of(
                    new KakaoOauthProvider(null, null),
                    new OauthProvider() {
                        @Override
                        public UserProfile getUserProfile(String accessToken) {
                            return null;
                        }

                        @Override
                        public boolean is(String name) {
                            return name.equals("google");
                        }
                    }
            )
    );

    @DisplayName("[애플리케이션에서 특정 프로바이더 액세스 토큰 조회용] provider이름으로 Oauth Provider를 조회한다.")
    @Test
    void testMapping() {
        //given
        String providerName = "kakao";

        //when
        OauthProvider oauthProvider = oauthProviders.mapping(providerName);

        //then
        assertSoftly(softly -> {
            assertThat(oauthProvider).isNotNull();
            assertThat(oauthProvider).isInstanceOf(KakaoOauthProvider.class);
        });
    }
}
