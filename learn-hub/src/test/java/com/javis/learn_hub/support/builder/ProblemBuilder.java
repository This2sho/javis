package com.javis.learn_hub.support.builder;

import com.javis.learn_hub.category.domain.Category;
import com.javis.learn_hub.member.domain.Member;
import com.javis.learn_hub.problem.domain.Difficulty;
import com.javis.learn_hub.problem.domain.Problem;
import com.javis.learn_hub.problem.domain.Visibility;
import com.javis.learn_hub.support.domain.Association;
import java.util.ArrayList;
import java.util.List;

public class ProblemBuilder {

    private Association<Category> categoryId = Association.from(1L);
    private Association<Problem> parentProblemId = Association.getEmpty();
    private Association<Member> writerId = Association.from(1L);
    private Difficulty difficulty = Difficulty.EASY;
    private String content = "기본 문제 내용";
    private Visibility visibility = Visibility.PRIVATE;

    public static ProblemBuilder builder() {
        return new ProblemBuilder();
    }

    public ProblemBuilder withCategoryId(Long categoryId) {
        this.categoryId = Association.from(categoryId);
        return this;
    }

    public ProblemBuilder withCategoryId(Association<Category> categoryId) {
        this.categoryId = categoryId;
        return this;
    }

    public ProblemBuilder withWriterId(Long writerId) {
        this.writerId = Association.from(writerId);
        return this;
    }

    public ProblemBuilder withWriterId(Association<Member> writerId) {
        this.writerId = writerId;
        return this;
    }

    public ProblemBuilder withParentProblemId(Long parentProblemId) {
        this.parentProblemId = Association.from(parentProblemId);
        return this;
    }

    public ProblemBuilder withParentProblemId(Association<Problem> parentProblemId) {
        this.parentProblemId = parentProblemId;
        return this;
    }

    public ProblemBuilder withDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
        return this;
    }

    public ProblemBuilder withContent(String content) {
        this.content = content;
        return this;
    }

    public ProblemBuilder withVisibility(Visibility visibility) {
        this.visibility = visibility;
        return this;
    }

    public Problem build() {
        return new Problem(categoryId, parentProblemId, writerId, difficulty, content, visibility);
    }

    public List<Problem> build(int size) {
        ArrayList<Problem> result = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            result.add(build());
        }
        return result;
    }
}

