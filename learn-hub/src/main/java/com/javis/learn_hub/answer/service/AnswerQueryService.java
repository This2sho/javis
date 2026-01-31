package com.javis.learn_hub.answer.service;

import com.javis.learn_hub.answer.domain.Answer;
import com.javis.learn_hub.answer.domain.repository.AnswerRepository;
import com.javis.learn_hub.answer.service.dto.AnswerStatusResponse;
import com.javis.learn_hub.evaluation.domain.Evaluation;
import com.javis.learn_hub.evaluation.domain.repository.EvaluationRepository;
import com.javis.learn_hub.support.domain.Association;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class AnswerQueryService {

    private final AnswerRepository answerRepository;
    private final EvaluationRepository evaluationRepository;

    @Transactional(readOnly = true)
    public AnswerStatusResponse getAnswerStatus(Long answerId) {
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new IllegalStateException("존재하지 않는 답변입니다: " + answerId));

        Optional<Evaluation> evaluation = evaluationRepository.findByAnswerId(Association.from(answerId));

        if (evaluation.isPresent()) {
            return AnswerStatusResponse.from(answer, evaluation.get());
        }
        return AnswerStatusResponse.pending(answer);
    }
}
