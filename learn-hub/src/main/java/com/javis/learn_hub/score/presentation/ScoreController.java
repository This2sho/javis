package com.javis.learn_hub.score.presentation;

import com.javis.learn_hub.score.service.ScoreService;
import com.javis.learn_hub.score.service.dto.CategoryScoreNodeResponse;
import com.javis.learn_hub.score.service.dto.ScoreSummaryResponse;
import com.javis.learn_hub.support.domain.Authenticated;
import com.javis.learn_hub.support.domain.MemberId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api")
@RestController
public class ScoreController {

    private final ScoreService scoreService;

    @GetMapping("/scores")
    public ResponseEntity<ScoreSummaryResponse> showScores(
            @Authenticated MemberId memberId
    ) {
        ScoreSummaryResponse scoreSummaryResponse = scoreService.showScores(memberId.getId());
        return ResponseEntity.ok().body(scoreSummaryResponse);
    }

    @GetMapping("/scores/{mainCategory}")
    public ResponseEntity<CategoryScoreNodeResponse> showDetailScores(
            @Authenticated MemberId memberId,
            @PathVariable String mainCategory
    ) {
        CategoryScoreNodeResponse response = scoreService.showDetailScore(memberId.getId(),
                mainCategory);
        return ResponseEntity.ok().body(response);
    }
}
