package com.javis.learn_hub.problem.domain.repository;

import com.javis.learn_hub.problem.domain.Problem;
import com.javis.learn_hub.problem.domain.ProblemScoringInfo;
import com.javis.learn_hub.support.domain.Association;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface ProblemScoringInfoRepository extends Repository<ProblemScoringInfo, Long> {

    ProblemScoringInfo save(ProblemScoringInfo problemScoringInfo);

    Optional<ProblemScoringInfo> findByProblemId(Association<Problem> problemId);

    @Query("""
        select psi
        from ProblemScoringInfo psi
        where psi.problemId in :problemIds
    """)
    List<ProblemScoringInfo> findAllByProblemIdIn(@Param("problemIds") List<Association<Problem>> problemIds);

    void deleteByProblemId(Association<Problem> problemId);

    @Query(value = "SELECT psi.* FROM problem_scoring_info psi "
            + "JOIN question q ON q.problem_id = psi.problem_id "
            + "WHERE q.id = :questionId",
            nativeQuery = true)
    Optional<ProblemScoringInfo> findByQuestionId(@Param("questionId") Long questionId);
}
