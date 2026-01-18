package com.javis.learn_hub.support;

import com.javis.learn_hub.answer.domain.Answer;
import com.javis.learn_hub.category.domain.Category;
import com.javis.learn_hub.interview.domain.Interview;
import com.javis.learn_hub.interview.domain.Question;
import com.javis.learn_hub.member.domain.Member;
import com.javis.learn_hub.problem.domain.Problem;
import com.javis.learn_hub.problem.domain.ProblemScoringInfo;
import com.javis.learn_hub.review.domain.Review;
import com.javis.learn_hub.score.domain.Score;
import com.javis.learn_hub.support.builder.ProblemBuilder;
import com.javis.learn_hub.support.repository.InMemoryAnswerRepository;
import com.javis.learn_hub.support.repository.InMemoryCategoryRepository;
import com.javis.learn_hub.support.repository.InMemoryInterviewRepository;
import com.javis.learn_hub.support.repository.InMemoryMemberRepository;
import com.javis.learn_hub.support.repository.InMemoryProblemRepository;
import com.javis.learn_hub.support.repository.InMemoryProblemScoringInfoRepository;
import com.javis.learn_hub.support.repository.InMemoryQuestionRepository;
import com.javis.learn_hub.support.repository.InMemoryRepository;
import com.javis.learn_hub.support.repository.InMemoryReviewRepository;
import com.javis.learn_hub.support.repository.InMemoryScoreRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TestFixtureFactory {

    private final InMemoryAnswerRepository answerRepository = new InMemoryAnswerRepository();
    private final InMemoryQuestionRepository questionRepository = new InMemoryQuestionRepository();
    private final InMemoryInterviewRepository interviewRepository = new InMemoryInterviewRepository();
    private final InMemoryProblemRepository problemRepository = new InMemoryProblemRepository();
    private final InMemoryScoreRepository scoreRepository = new InMemoryScoreRepository();
    private final InMemoryCategoryRepository categoryRepository = new InMemoryCategoryRepository();
    private final InMemoryMemberRepository memberRepository = new InMemoryMemberRepository();
    private final InMemoryProblemScoringInfoRepository problemScoringInfoRepository = new InMemoryProblemScoringInfoRepository();
    private final InMemoryReviewRepository reviewRepository = new InMemoryReviewRepository();

    private final Map<Class<?>, InMemoryRepository<?>> repositoryMap = Map.of(
            Answer.class, answerRepository,
            Question.class, questionRepository,
            Interview.class, interviewRepository,
            Problem.class, problemRepository,
            ProblemScoringInfo.class, problemScoringInfoRepository,
            Score.class, scoreRepository,
            Category.class, categoryRepository,
            Member.class, memberRepository,
            Review.class, reviewRepository
    );

    public <T> T make(T entity) {
        InMemoryRepository<T> repository = (InMemoryRepository<T>) repositoryMap.get(entity.getClass());
        if (repository == null) {
            throw new IllegalArgumentException("지원하지 않는 엔티티 타입: " + entity.getClass());
        }
        repository.save(entity);
        return entity;
    }

    public List<Problem> make5ProblemsWithCategory(Category category) {
        ArrayList<Problem> result = new ArrayList<>();
        for (int i=0; i<5; i++) {
            result.add(make(ProblemBuilder.builder().withCategoryId(category.getId()).build()));
        }
        return result;
    }

    public InMemoryAnswerRepository getAnswerRepository() {
        return answerRepository;
    }

    public InMemoryQuestionRepository getQuestionRepository() {
        return questionRepository;
    }

    public InMemoryInterviewRepository getInterviewRepository() {
        return interviewRepository;
    }

    public InMemoryProblemRepository getProblemRepository() {
        return problemRepository;
    }

    public InMemoryScoreRepository getScoreRepository() {
        return scoreRepository;
    }

    public InMemoryCategoryRepository getCategoryRepository() {
        return categoryRepository;
    }

    public InMemoryMemberRepository getMemberRepository() {
        return memberRepository;
    }

    public InMemoryProblemScoringInfoRepository getProblemScoringInfoRepository() {
        return problemScoringInfoRepository;
    }

    public InMemoryReviewRepository getReviewRepository() {
        return reviewRepository;
    }
}
