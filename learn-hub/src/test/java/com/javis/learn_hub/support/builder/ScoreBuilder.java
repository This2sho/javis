package com.javis.learn_hub.support.builder;

import com.javis.learn_hub.category.domain.Category;
import com.javis.learn_hub.member.domain.Member;
import com.javis.learn_hub.score.domain.Score;
import com.javis.learn_hub.support.domain.Association;

public class ScoreBuilder {

    private Association<Member> memberId = Association.from(1L);
    private Association<Category> categoryId = Association.from(1L);
    private int score = 0;

    public static ScoreBuilder builder() {
        return new ScoreBuilder();
    }

    public ScoreBuilder withMemberId(Long memberId) {
        this.memberId = Association.from(memberId);
        return this;
    }

    public ScoreBuilder withMemberId(Association<Member> memberId) {
        this.memberId = memberId;
        return this;
    }

    public ScoreBuilder withCategoryId(Long categoryId) {
        this.categoryId = Association.from(categoryId);
        return this;
    }

    public ScoreBuilder withCategoryId(Association<Category> categoryId) {
        this.categoryId = categoryId;
        return this;
    }

    public ScoreBuilder withScore(int score) {
        this.score = score;
        return this;
    }

    public Score build() {
        Score score = new Score(memberId, categoryId);
        score.addScore(this.score); // 초기 스코어 설정
        return score;
    }
}

