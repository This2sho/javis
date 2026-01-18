package com.javis.learn_hub.support.exception;

import com.javis.learn_hub.support.exception.dto.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(IllegalStateException e) {
        return ResponseEntity
                .badRequest()
                .body(new ErrorResponse(e.getMessage()));
    }
}
