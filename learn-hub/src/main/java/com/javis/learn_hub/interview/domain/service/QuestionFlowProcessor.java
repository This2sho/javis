package com.javis.learn_hub.interview.domain.service;

import com.javis.learn_hub.interview.domain.Interview;
import com.javis.learn_hub.interview.domain.Question;
import com.javis.learn_hub.interview.domain.repository.QuestionRepository;
import com.javis.learn_hub.interview.domain.service.dto.NextQuestionResult;
import com.javis.learn_hub.problem.domain.Difficulty;
import com.javis.learn_hub.problem.domain.Problem;
import com.javis.learn_hub.problem.domain.service.ProblemRecommender;
import com.javis.learn_hub.support.domain.Association;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class QuestionFlowProcessor {

    private final QuestionRepository questionRepository;
    private final InterviewReader interviewReader;
    private final ProblemRecommender problemRecommender;

    public List<Question> createRootQuestions(List<Problem> problems, Long interviewId) {
        List<Question> rootQuestions = IntStream.range(0, problems.size())
                .mapToObj(index -> Question.rootQuestionOf(
                        Association.from(problems.get(index).getId()),
                        Association.from(interviewId),
                        index,
                        problems.get(index).getContent())
                ).toList();
        questionRepository.saveAll(rootQuestions);
        return rootQuestions;
    }

    public void markQuestionAnswered(Long questionId) {
        Question question = interviewReader.getQuestion(questionId);
        question.markAnswered();
        questionRepository.save(question);
    }

    public NextQuestionResult continueNextQuestion(Long questionId, List<Difficulty> preferences) {
        Question previousQuestion = interviewReader.getQuestion(questionId);
        Interview interview = interviewReader.get(previousQuestion.getInterviewId());
        Optional<Question> nextQuestion = proceedToFollowUpQuestion(previousQuestion, preferences)
                .or(() -> proceedToNextRootQuestion(interview));

        if (nextQuestion.isPresent()) {
            return NextQuestionResult.withNextQuestion(interview, nextQuestion.get());
        }

        return NextQuestionResult.finished(interview);
    }

    private Optional<Question> proceedToFollowUpQuestion(Question previousQuestion, List<Difficulty> preferences) {
        List<Association<Problem>> answeredProblemIds = interviewReader.getAllAnsweredProblemIds(
                previousQuestion.getInterviewId());
        return problemRecommender
                .recommendNextProblem(previousQuestion.getProblemId(), answeredProblemIds, preferences)
                .map(problem -> createFollowUpQuestion(previousQuestion, problem));
    }

    private Question createFollowUpQuestion(Question previousQuestion, Problem problem) {
        Question question = previousQuestion.makeFollowUpQuestion(problem);
        questionRepository.save(question);
        return question;
    }

    private Optional<Question> proceedToNextRootQuestion(Interview interview) {
        if (interview.hasNextQuestion()) {
            interview.moveNextQuestion();
            int questionOrder = interview.getCurrentQuestionOrder();
            return questionRepository.findByInterviewIdAndParentQuestionIdAndQuestionOrder(
                    Association.from(interview.getId()),
                    Association.getEmpty(),
                    questionOrder
            );
        }
        return Optional.empty();
    }
}
