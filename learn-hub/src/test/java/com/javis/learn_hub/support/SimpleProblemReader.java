package com.javis.learn_hub.support;

import com.javis.learn_hub.problem.domain.repository.ProblemRepository;
import com.javis.learn_hub.problem.domain.service.ProblemReader;
import com.javis.learn_hub.support.repository.InMemoryProblemScoringInfoRepository;

public class SimpleProblemReader extends ProblemReader {

    public SimpleProblemReader(ProblemRepository problemRepository) {
        super(problemRepository, new InMemoryProblemScoringInfoRepository());
    }
}
