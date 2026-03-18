package com.javis.learn_hub.answer.domain;

import java.util.Collections;
import java.util.Set;

public enum EvaluationState {

    PENDING {
        @Override
        public Set<EvaluationState> nextStates() {
            return Set.of(SCORING);
        }
    },

    SCORING {
        @Override
        public Set<EvaluationState> nextStates() {
            return Set.of(SCORED, FAILED);
        }
    },

    FAILED {
        @Override
        public Set<EvaluationState> nextStates() {
            return Set.of(SCORING);
        }
    },

    SCORED;

    public Set<EvaluationState> nextStates() {
        return Collections.emptySet();
    }

    public boolean canChangeTo(EvaluationState next) {
        return nextStates().contains(next);
    }
}
