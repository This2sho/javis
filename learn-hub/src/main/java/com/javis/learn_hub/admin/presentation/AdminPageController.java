package com.javis.learn_hub.admin.presentation;

import com.javis.learn_hub.member.domain.Role;
import com.javis.learn_hub.review.service.ReviewQueryService;
import com.javis.learn_hub.support.domain.Authenticated;
import com.javis.learn_hub.support.domain.MemberId;
import com.javis.learn_hub.support.domain.RequireRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@RequiredArgsConstructor
@RequestMapping("/admin")
@Controller
public class AdminPageController {

    private final ReviewQueryService reviewQueryService;

    @RequireRole(value = Role.ADMIN)
    @GetMapping()
    public String mainPage(@Authenticated MemberId memberId) {
        return "admin-main";
    }

    @RequireRole(value = Role.ADMIN)
    @GetMapping("/review-requests")
    public String reviewListPage(@Authenticated MemberId memberId) {
        return "admin-review-requests";
    }

    @RequireRole(value = Role.ADMIN)
    @GetMapping("/review-requests/{reviewId}")
    public String reviewDetailPage(
            @Authenticated MemberId memberId,
            @PathVariable Long reviewId,
            Model model
    ) {
        Long problemId = reviewQueryService.getRootProblemId(reviewId);
        model.addAttribute("reviewId", reviewId);
        model.addAttribute("problemId", problemId);
        return "admin-review-detail";
    }

    @RequireRole(value = Role.ADMIN)
    @GetMapping("/problem-insert")
    public String problemInsertPage(@Authenticated MemberId memberId) {
        return "admin-problem-insert";
    }
}
