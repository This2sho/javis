package com.javis.learn_hub.evaluation.application;

import com.javis.learn_hub.answer.domain.Answer;
import com.javis.learn_hub.answer.domain.service.AnswerReader;
import com.javis.learn_hub.answer.service.AnswerCommandService;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadPoolExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class EvaluationQueuePoller implements SchedulingConfigurer {

    private static final long MIN_DELAY_MS = 100;
    private static final long MAX_DELAY_MS = 1500;
    private static final long INITIAL_DELAY_MS = 500;

    private volatile long currentDelayMs = INITIAL_DELAY_MS;

    private final AnswerReader answerReader;
    private final AnswerCommandService answerCommandService;
    private final EvaluationFacade evaluationFacade;
    private final ThreadPoolTaskExecutor evaluationExecutor;

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.addTriggerTask(
                this::pollEvaluationQueue,
                this::nextExecution
        );
    }

    public void pollEvaluationQueue() {
        PollDecision decision = decideNextPolling();
        applyDelay(decision);
    }

    private PollDecision decideNextPolling() {
        int fetchLimit = calculateFetchLimit();
        if (fetchLimit <= 0) {
            return PollDecision.POLL_SOON;
        }

        List<Answer> candidates = answerReader.getQueuedForEvaluation(fetchLimit);
        if (candidates.isEmpty()) {
            return PollDecision.POLL_LATER;
        }

        return submitCandidates(candidates);
    }

    private int calculateFetchLimit() {
        ThreadPoolExecutor executor = evaluationExecutor.getThreadPoolExecutor();
        int maxPoolSize = executor.getMaximumPoolSize();
        int activeCount = executor.getActiveCount();
        int availableCapacity = Math.max(0, maxPoolSize - activeCount);

        return availableCapacity;
    }

    private PollDecision submitCandidates(List<Answer> candidates) {
        boolean hasSubmitted = false;

        for (Answer candidate : candidates) {
            Optional<Answer> claimed = answerCommandService.prepareScoring(candidate.getId());
            if (claimed.isEmpty()) {
                continue;
            }

            Answer answer = claimed.get();
            if (!submitClaimedAnswer(answer)) {
                return PollDecision.POLL_SOON;
            }

            hasSubmitted = true;
        }

        return hasSubmitted ? PollDecision.POLL_SOON : PollDecision.POLL_LATER;
    }

    private boolean submitClaimedAnswer(Answer answer) {
        if (submit(answer)) {
            return true;
        }

        answerCommandService.requeueEvaluation(answer.getId());
        return false;
    }

    private boolean submit(Answer answer) {
        Long questionId = answer.getQuestionId().getId();

        try {
            evaluationExecutor.execute(() -> evaluationFacade.processEvaluation(answer, questionId));
            return true;
        } catch (TaskRejectedException e) {
            log.debug("evaluationExecutor 포화: answerId={}", answer.getId());
            return false;
        }
    }

    private void applyDelay(PollDecision decision) {
        switch (decision) {
            case POLL_SOON -> pollSoon();
            case POLL_LATER -> pollLater();
        }
    }

    private void pollSoon() {
        currentDelayMs = MIN_DELAY_MS;
    }

    private void pollLater() {
        currentDelayMs = Math.min(MAX_DELAY_MS, currentDelayMs * 2);
    }

    private Instant nextExecution(TriggerContext triggerContext) {
        Instant base = triggerContext.lastCompletion();
        if (base == null) {
            base = Instant.now();
        }
        return base.plusMillis(currentDelayMs);
    }

    private enum PollDecision {
        POLL_SOON,
        POLL_LATER
    }
}
