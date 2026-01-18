package com.javis.learn_hub.answer.domain.service;

import com.javis.learn_hub.answer.domain.Answer;
import com.javis.learn_hub.answer.domain.service.dto.CategoryGrade;
import com.javis.learn_hub.answer.domain.service.dto.QnA;
import com.javis.learn_hub.interview.domain.Interview;
import com.javis.learn_hub.interview.domain.Question;
import com.javis.learn_hub.interview.domain.service.InterviewReader;
import com.javis.learn_hub.problem.domain.Problem;
import com.javis.learn_hub.problem.domain.service.ProblemReader;
import com.javis.learn_hub.support.domain.Association;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class AnswerFinder {

    private final AnswerReader answerReader;
    private final InterviewReader interviewReader;
    private final ProblemReader problemReader;

    public List<CategoryGrade> findCategoryGrades(Association<Interview> interviewId) {
        List<Question> questions = interviewReader.getAllQuestions(interviewId);
        List<QnA> qnAs = findQnA(questions);
        List<Problem> problems = problemReader.getAll(toProblemIds(questions));
        return toCategoryGrades(qnAs, problems);
    }

    public List<QnA> findQnA(List<Question> questions) {
        List<Answer> answers = answerReader.getAll(toQuestionIds(questions));
        return toQnAs(questions, answers);
    }

    private List<Association<Question>> toQuestionIds(List<Question> questions) {
        return questions.stream()
                .map(question -> Association.<Question>from(question.getId()))
                .toList();
    }

    private List<Long> toProblemIds(List<Question> questions) {
        return questions.stream()
                .map(question -> question.getProblemId().getId())
                .toList();
    }

    private List<QnA> toQnAs(List<Question> questions, List<Answer> answers) {
        Map<Long, Question> questionMap = questions.stream()
                .collect(Collectors.toMap(question -> question.getId(), question -> question));
        return answers.stream()
                .map(answer -> {
                    Question question = questionMap.get(answer.getQuestionId().getId());
                    return new QnA(question, answer);
                }).toList();
    }

    private List<CategoryGrade> toCategoryGrades(List<QnA> qnAs, List<Problem> problems) {
        Map<Long, Long> categoriesByProblemId = problems.stream()
                .collect(Collectors.toMap(
                        problem -> problem.getId(),
                        problem -> problem.getCategoryId().getId()
                ));
        return qnAs.stream()
                .map(qna -> new CategoryGrade(
                        categoriesByProblemId.get(qna.question().getProblemId().getId()),
                        qna.answer().getEvaluationResult().getGrade()
                        )
                ).toList();
    }
}
