package com.javis.learn_hub.support.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ProviderTest {

    @DisplayName("[소설 가입 시 회원 식별 용도] 소설 로그인 Provider 이름으로 Provider를 가져온다.")
    @Test
    void testFind() {
        //given
        String providerName = "kakao";

        //when
        Provider provider = Provider.find(providerName);

        //then
        assertThat(provider).isEqualTo(Provider.KAKAO);
    }
}
