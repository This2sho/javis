package com.javis.learn_hub.support.repository;

import com.javis.learn_hub.problem.domain.Problem;
import com.javis.learn_hub.problem.domain.ProblemScoringInfo;
import com.javis.learn_hub.problem.domain.repository.ProblemScoringInfoRepository;
import com.javis.learn_hub.support.domain.Association;
import java.util.Optional;

public class InMemoryProblemScoringInfoRepository extends InMemoryRepository<ProblemScoringInfo> implements
        ProblemScoringInfoRepository {

    @Override
    public Optional<ProblemScoringInfo> findByProblemId(Association<Problem> problemId) {
        return findOne(psi -> psi.getProblemId().equals(problemId));
    }
}
