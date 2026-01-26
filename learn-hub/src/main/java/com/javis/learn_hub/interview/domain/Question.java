package com.javis.learn_hub.interview.domain;

import com.javis.learn_hub.problem.domain.Problem;
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
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@EqualsAndHashCode(of = {"id"}, callSuper = false)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        indexes = {
                @Index(
                        name = "idx_question_interview_id_question_status",
                        columnList = "interview_id, question_status"
                ),
                @Index(
                        name = "idx_question_interview_id_parent_question_id_question_order",
                        columnList = "interview_id, parent_question_id, question_order"
                )
        }
)
public class Question extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Convert(converter = AssociationConverter.class)
    private Association<Problem> problemId;

    @Convert(converter = AssociationConverter.class)
    private Association<Interview> interviewId;

    @Convert(converter = AssociationConverter.class)
    private Association<Question> parentQuestionId;

    private int questionOrder;

    @Enumerated(EnumType.STRING)
    private QuestionStatus questionStatus;

    @Lob
    private String message;

    public Question(Association<Problem> problemId, Association<Interview> interviewId,
                    Association<Question> parentQuestionId, int questionOrder, String message) {
        this.problemId = problemId;
        this.interviewId = interviewId;
        this.parentQuestionId = parentQuestionId;
        this.questionOrder = questionOrder;
        this.message = message;
        this.questionStatus = QuestionStatus.UNANSWERED;
    }

    public static Question rootQuestionOf(
            Association<Problem> problemId, Association<Interview> interviewId,
            int questionOrder, String message
    ) {
        return new Question(problemId, interviewId, Association.getEmpty(), questionOrder, message);
    }

    public Question makeFollowUpQuestion(Problem problem) {
        return new Question(Association.from(problem.getId()), interviewId, Association.from(this.id), 0,
                problem.getContent());
    }

    public void complete() {
        this.questionStatus = QuestionStatus.ANSWERED;
    }

    public boolean isFollowUpQuestion() {
        return this.parentQuestionId != Association.getEmpty();
    }
}
