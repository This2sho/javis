package com.javis.learn_hub.support.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.javis.learn_hub.support.exception.dto.ErrorResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();

    @DisplayName("IllegalArgumentException은 message 필드를 가진 400 응답으로 변환한다.")
    @Test
    void testHandleIllegalArgumentException() {
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleIllegalArgumentException(
                new IllegalArgumentException("존재하지 않는 채점 결과입니다.")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(new ErrorResponse("존재하지 않는 채점 결과입니다."));
    }
}
