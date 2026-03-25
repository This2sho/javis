package com.javis.learn_hub.support.builder;

import com.javis.learn_hub.problem.domain.Problem;
import com.javis.learn_hub.problem.domain.ProblemScoringInfo;
import com.javis.learn_hub.support.domain.Association;
import java.util.ArrayList;
import java.util.List;

public class ProblemScoringInfoBuilder {

    private Association<Problem> problemId = Association.from(1L);
    private String referenceAnswer = "예상 답변";

    public static ProblemScoringInfoBuilder builder() {
        return new ProblemScoringInfoBuilder();
    }

    public ProblemScoringInfoBuilder withProblemId(Long problemId) {
        this.problemId = Association.from(problemId);
        return this;
    }

    public ProblemScoringInfoBuilder withProblemId(Association<Problem> problemId) {
        this.problemId = problemId;
        return this;
    }

    public ProblemScoringInfoBuilder withReferenceAnswer(String referenceAnswer) {
        this.referenceAnswer = referenceAnswer;
        return this;
    }

    public ProblemScoringInfo build() {
        return new ProblemScoringInfo(problemId, referenceAnswer);
    }

    public List<ProblemScoringInfo> build(int size) {
        ArrayList<ProblemScoringInfo> result = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            result.add(build());
        }
        return result;
    }
}
