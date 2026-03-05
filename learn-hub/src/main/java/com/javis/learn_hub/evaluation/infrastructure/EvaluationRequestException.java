package com.javis.learn_hub.evaluation.infrastructure;

public class EvaluationRequestException extends RuntimeException {

    public EvaluationRequestException(Long answerId) {
        super("채점 요청 최종 실패: answerId=" + answerId);
    }
}
