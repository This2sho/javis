package com.javis.learn_hub.category.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CategoryTest {

    @DisplayName("메인 카테고리와 서브 카테고리들을 소문자로 만들어 카테고리 경로를 생성한다.")
    @Test
    void testOf() {
        //given
        MainCategory mainCategory = MainCategory.COMPUTER_SCIENCE;
        String network = "network";
        String tcp = "tcp";

        //when
        Category actual = Category.of(mainCategory, network, tcp);

        //then
        assertThat(actual.getPath()).contains(mainCategory.name().toLowerCase(), network, tcp);
    }
}
