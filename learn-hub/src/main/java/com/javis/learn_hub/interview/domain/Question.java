package com.javis.learn_hub.interview.domain;

import com.javis.learn_hub.problem.domain.Problem;
import com.javis.learn_hub.support.domain.Association;
import com.javis.learn_hub.support.domain.BaseEntity;
import com.javis.learn_hub.support.infrastructure.AssociationConverter;
import com.javis.learn_hub.support.i18n.ContentLanguage;
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

    private int depth;

    private int questionOrder;

    @Enumerated(EnumType.STRING)
    private QuestionStatus questionStatus;

    @Enumerated(EnumType.STRING)
    private ContentLanguage contentLanguage;

    @Lob
    private String message;

    public Question(Association<Problem> problemId, Association<Interview> interviewId,
                    Association<Question> parentQuestionId, int depth, int questionOrder, String message,
                    ContentLanguage contentLanguage) {
        this.problemId = problemId;
        this.interviewId = interviewId;
        this.parentQuestionId = parentQuestionId;
        this.depth = depth;
        this.questionOrder = questionOrder;
        this.message = message;
        this.contentLanguage = contentLanguage;
        this.questionStatus = QuestionStatus.UNANSWERED;
    }

    public Question(Association<Problem> problemId, Association<Interview> interviewId,
                    Association<Question> parentQuestionId, int depth, int questionOrder, String message) {
        this(problemId, interviewId, parentQuestionId, depth, questionOrder, message, ContentLanguage.KO);
    }

    public static Question rootQuestionOf(
            Association<Problem> problemId, Association<Interview> interviewId,
            int questionOrder, String message, ContentLanguage contentLanguage
    ) {
        return new Question(problemId, interviewId, Association.getEmpty(), 0, questionOrder, message, contentLanguage);
    }

    public static Question rootQuestionOf(
            Association<Problem> problemId, Association<Interview> interviewId,
            int questionOrder, String message
    ) {
        return rootQuestionOf(problemId, interviewId, questionOrder, message, ContentLanguage.KO);
    }

    public Question makeFollowUpQuestion(Problem problem) {
        return new Question(Association.from(problem.getId()), interviewId, Association.from(this.id), this.depth + 1, 0,
                problem.getContent(), problem.getContentLanguage());
    }

    public void markAnswered() {
        this.questionStatus = QuestionStatus.ANSWERED;
    }

    public boolean isFollowUpQuestion() {
        return depth > 0;
    }

    public boolean isNotAnswered() {
        return questionStatus == QuestionStatus.UNANSWERED;
    }
}
