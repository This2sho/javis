package com.javis.learn_hub.problem.domain.service.dto;

import com.javis.learn_hub.problem.domain.Problem;
import com.javis.learn_hub.problem.domain.ProblemScoringInfo;

public record ProblemDetailView(
        Problem problem,
        ProblemScoringInfo problemScoringInfo
) {

}
