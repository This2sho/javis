package com.javis.learn_hub.support;

import com.javis.learn_hub.category.domain.service.CategoryReader;
import com.javis.learn_hub.score.domain.repository.ScoreRepository;
import com.javis.learn_hub.score.domain.service.ScoreReader;

public class SimpleScoreReader extends ScoreReader {

    public SimpleScoreReader(ScoreRepository scoreRepository, CategoryReader categoryReader) {
        super(scoreRepository, categoryReader);
    }
}
