package com.javis.learn_hub.answer.domain;

public enum EvaluationStatus {

    PENDING {
        @Override
        public EvaluationStatus toScoring() {
            return SCORING;
        }
    },

    SCORING {
        @Override
        public EvaluationStatus success() {
            return SCORED;
        }

        @Override
        public EvaluationStatus fail() {
            return FAILED;
        }
    },

    FAILED {
        @Override
        public EvaluationStatus toScoring() {
            return SCORING;
        }
    },

    SCORED;

    public EvaluationStatus toScoring() {
        throw new IllegalStateException(this + " 상태에서 채점 요청 불가");
    }

    public EvaluationStatus success() {
        throw new IllegalStateException(this + " 상태에서 채점 완료 처리 불가");
    }

    public EvaluationStatus fail() {
        throw new IllegalStateException(this + " 상태에서 채점 실패 처리 불가");
    }
}
