package com.javis.learn_hub.answer.domain;

public enum EvaluationState {
    PENDING, SCORING, SCORED, FAILED;

    public EvaluationState toScoring() {
        if (this == PENDING || this == FAILED) return SCORING;
        throw new IllegalStateException(this + " 상태에서는 채점을 시작할 수 없습니다.");
    }

    public EvaluationState success() {
        if (this == SCORING) return SCORED;
        throw new IllegalStateException(this + " 상태에서는 완료 처리가 불가합니다.");
    }

    public EvaluationState fail() {
        if (this == SCORING) return FAILED;
        throw new IllegalStateException(this + " 상태에서는 실패 처리가 불가합니다.");
    }
}
