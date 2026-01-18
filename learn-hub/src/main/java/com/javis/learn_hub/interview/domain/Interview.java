package com.javis.learn_hub.interview.domain;

import com.javis.learn_hub.category.domain.MainCategory;
import com.javis.learn_hub.member.domain.Member;
import com.javis.learn_hub.support.domain.Association;
import com.javis.learn_hub.support.domain.BaseEntity;
import com.javis.learn_hub.support.infrastructure.AssociationConverter;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Interview extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Convert(converter = AssociationConverter.class)
    private Association<Member> memberId;

    @Enumerated(EnumType.STRING)
    private MainCategory mainCategory;

    @Enumerated(EnumType.STRING)
    InterviewStatus status;

    private int currentQuestionOrder;

    private int rootQuestionSize;

    public Interview(Association<Member> memberId, MainCategory mainCategory, int rootQuestionSize) {
        this.memberId = memberId;
        this.mainCategory = mainCategory;
        this.status = InterviewStatus.ACTIVE;
        this.currentQuestionOrder = 0;
        this.rootQuestionSize = rootQuestionSize;
    }

    public boolean hasNextQuestion(){
        return currentQuestionOrder + 1 < this.rootQuestionSize;
    }

    public void moveNextQuestion() {
        if (hasNextQuestion()) {
            this.currentQuestionOrder++;
            return;
        }
        throw new IllegalStateException("다음 질문이 존재하지 않습니다.");
    }

    public void finish() {
        status = InterviewStatus.ENDED;
    }

    public boolean isFinished() {
        return status == InterviewStatus.ENDED;
    }
}
