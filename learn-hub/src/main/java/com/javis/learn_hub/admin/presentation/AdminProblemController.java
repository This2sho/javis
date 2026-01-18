package com.javis.learn_hub.admin.presentation;

import com.javis.learn_hub.member.domain.Role;
import com.javis.learn_hub.problem.service.ProblemCommandService;
import com.javis.learn_hub.problem.service.dto.ProblemCreateRequest;
import com.javis.learn_hub.support.domain.Authenticated;
import com.javis.learn_hub.support.domain.MemberId;
import com.javis.learn_hub.support.domain.RequireRole;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/admin/api")
@RestController
public class AdminProblemController {

    private final ProblemCommandService problemCommandService;

    @RequireRole(value = Role.ADMIN)
    @PostMapping("/problems")
    public ResponseEntity<Void> insertProblems(
            @RequestBody List<ProblemCreateRequest> requests,
            @Authenticated MemberId memberId
    ) {
        problemCommandService.createAll(requests, memberId.getId());
        return ResponseEntity.ok()
                .location(URI.create("/problems"))
                .build();
    }
}
