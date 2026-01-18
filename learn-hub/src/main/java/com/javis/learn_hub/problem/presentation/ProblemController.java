package com.javis.learn_hub.problem.presentation;

import com.javis.learn_hub.problem.service.ProblemCommandService;
import com.javis.learn_hub.problem.service.ProblemQueryService;
import com.javis.learn_hub.problem.service.dto.ProblemCreateRequest;
import com.javis.learn_hub.problem.service.dto.ProblemHistoryDetailResponse;
import com.javis.learn_hub.problem.service.dto.ProblemHistoryResponse;
import com.javis.learn_hub.problem.service.dto.ProblemUpdateRequest;
import com.javis.learn_hub.support.application.dto.CursorPageRequest;
import com.javis.learn_hub.support.application.dto.CursorPageResponse;
import com.javis.learn_hub.support.domain.Authenticated;
import com.javis.learn_hub.support.domain.MemberId;
import com.javis.learn_hub.support.presentation.WithCursor;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api")
@RequiredArgsConstructor
@RestController
public class ProblemController {

    private final ProblemCommandService problemCommandService;
    private final ProblemQueryService problemQueryService;

    @PostMapping("/problems")
    public ResponseEntity<Void> createProblem(
            @RequestBody ProblemCreateRequest request,
            @Authenticated MemberId memberId
    ) {
        Long problemId = problemCommandService.create(request, memberId.getId());
        return ResponseEntity.created(URI.create("/problems/" + problemId)).build();
    }

    @GetMapping("/problems")
    public ResponseEntity<CursorPageResponse<ProblemHistoryResponse>> viewProblems(
            @WithCursor(requiredTargetId = false, requiredTargetTime = false) CursorPageRequest cursorPageRequest,
            @Authenticated MemberId memberId
    ) {
        CursorPageResponse<ProblemHistoryResponse> result = problemQueryService.viewHistories(memberId.getId(), cursorPageRequest);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/problems/{problemId}")
    public ResponseEntity<ProblemHistoryDetailResponse> viewProblemDetail(
            @PathVariable Long problemId,
            @Authenticated MemberId memberId
    ) {
        ProblemHistoryDetailResponse response = problemQueryService.viewHistory(problemId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/problems/{problemId}")
    public ResponseEntity<Void> updateProblem(
            @RequestBody ProblemUpdateRequest request,
            @PathVariable Long problemId,
            @Authenticated MemberId memberId
    ) {
        problemCommandService.update(request, memberId.getId());
        return ResponseEntity.noContent().build();
    }
}
