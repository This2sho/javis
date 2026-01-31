package com.javis.learn_hub.evaluation.domain.repository;

import com.javis.learn_hub.answer.domain.Answer;
import com.javis.learn_hub.evaluation.domain.Evaluation;
import com.javis.learn_hub.support.domain.Association;
import java.util.Optional;
import org.springframework.data.repository.Repository;

public interface EvaluationRepository extends Repository<Evaluation, Long> {

    Evaluation save(Evaluation evaluation);

    Optional<Evaluation> findById(Long id);

    Optional<Evaluation> findByAnswerId(Association<Answer> answerId);
}
