package com.javis.learn_hub.interview.domain.service;

import com.javis.learn_hub.category.domain.MainCategory;
import com.javis.learn_hub.event.DomainEvent;
import com.javis.learn_hub.event.InterviewFinishEvent;
import com.javis.learn_hub.interview.domain.EmptyProblemException;
import com.javis.learn_hub.interview.domain.Interview;
import com.javis.learn_hub.interview.domain.Question;
import com.javis.learn_hub.interview.domain.repository.InterviewRepository;
import com.javis.learn_hub.interview.domain.repository.QuestionRepository;
import com.javis.learn_hub.problem.domain.Difficulty;
import com.javis.learn_hub.problem.domain.Problem;
import com.javis.learn_hub.problem.domain.service.ProblemRecommender;
import com.javis.learn_hub.support.domain.Association;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class InterviewProcessor {

    private static final int STARTING_PROBLEM_SIZE = 5;

    private final InterviewRepository interviewRepository;
    private final QuestionRepository questionRepository;

    private final InterviewReader interviewReader;
    private final ProblemRecommender problemRecommender;

    public List<Question> initInterview(MainCategory mainCategory, Long memberId) {
        List<Problem> rootProblems = problemRecommender.recommendRootProblems(memberId, mainCategory,
                STARTING_PROBLEM_SIZE);
        if (rootProblems.isEmpty()) {
            throw new EmptyProblemException();
        }
        Interview interview = new Interview(Association.from(memberId), mainCategory, rootProblems.size());
        interviewRepository.save(interview);
        return createRootQuestions(rootProblems, interview.getId());
    }

    private List<Question> createRootQuestions(List<Problem> problems, Long interviewId) {
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

    public Optional<Question> proceedToFollowUpQuestion(
            Question previousQuestion,
            List<Difficulty> preferences
    ) {
        List<Association<Problem>> answeredProblemIds = interviewReader.getAllAnsweredProblemIds(
                previousQuestion.getInterviewId());
        Optional<Problem> nextProblem = problemRecommender
                .recommendNextProblem(previousQuestion.getProblemId(), answeredProblemIds, preferences);
        if (nextProblem.isPresent()) {
            Question followUpQuestion = createFollowUpQuestion(previousQuestion, nextProblem.get());
            return Optional.of(followUpQuestion);
        }
        return Optional.empty();
    }

    private Question createFollowUpQuestion(Question previousQuestion, Problem problem) {
        Question question = previousQuestion.makeFollowUpQuestion(problem);
        questionRepository.save(question);
        return question;
    }

    public Optional<Question> proceedToNextRootQuestion(Interview interview) {
        if (interview.hasNextQuestion()) {
            interview.moveNextQuestion();
            int questionOrder = interview.getCurrentQuestionOrder();
            return questionRepository.findByInterviewIdAndParentQuestionIdAndQuestionOrder(
                    Association.from(interview.getId()),
                    Association.getEmpty()
                    , questionOrder);
        }
        return Optional.empty();
    }

    public void markQuestionAnswered(Question question) {
        question.markAnswered();
        questionRepository.save(question);
    }

    public void markQuestionCompleted(Question question) {
        question.markCompleted();
        questionRepository.save(question);
    }


    public List<DomainEvent> finish(Interview interview) {
        interview.finish();
        List<DomainEvent> events = new ArrayList<>();
        events.add(new InterviewFinishEvent(interview.getId(), interview.getMemberId().getId()));
        return events;
    }
}
