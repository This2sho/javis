package com.javis.learn_hub.evaluation.infrastructure;

public class EvaluationRequestException extends RuntimeException {

    public EvaluationRequestException(Exception e) {
        super("채점 요청 최종 실패: {}" + e.getMessage());
    }
}
