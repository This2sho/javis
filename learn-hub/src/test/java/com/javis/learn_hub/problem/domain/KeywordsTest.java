package com.javis.learn_hub.problem.domain;

import java.util.Collections;
import java.util.Set;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class KeywordsTest {

    @DisplayName("키워드 생성시 아무 키워드를 입력하지 않을 경우 예외를 던진다.")
    @Test
    void testValidate() {
        //given
        Set<String> emptyInput = Collections.emptySet();

        //when, then
        Assertions.assertThatThrownBy(() -> Keywords.from(emptyInput))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("키워드 생성시 ',' 문자 포함시 예외를 던진다.")
    @Test
    void testValidate2() {
        //given
        Set<String> emptyInput = Set.of("키워드1, 키워드2");

        //when, then
        Assertions.assertThatThrownBy(() -> Keywords.from(emptyInput))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
