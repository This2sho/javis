package com.javis.learn_hub.problem.domain;

import com.javis.learn_hub.support.builder.ProblemBuilder;
import com.javis.learn_hub.support.domain.Association;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ProblemTest {

    @DisplayName("작성자가 다른 경우 예외를 던진다.")
    @Test
    void testValidateWriter() {
        //given
        Problem problem = ProblemBuilder.builder().withWriterId(10L).build();

        //when, then
        Assertions.assertThatThrownBy(() -> problem.validateWriter(Association.from(1L)))
                .isInstanceOf(IllegalArgumentException.class);

    }
}
