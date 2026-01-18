package com.javis.learn_hub.problem.domain.repository;

import com.javis.learn_hub.problem.domain.Problem;
import com.javis.learn_hub.problem.domain.ProblemScoringInfo;
import com.javis.learn_hub.support.domain.Association;
import java.util.Optional;
import org.springframework.data.repository.Repository;

public interface ProblemScoringInfoRepository extends Repository<ProblemScoringInfo, Long> {

    ProblemScoringInfo save(ProblemScoringInfo problemScoringInfo);

    Optional<ProblemScoringInfo> findByProblemId(Association<Problem> problemId);
}

