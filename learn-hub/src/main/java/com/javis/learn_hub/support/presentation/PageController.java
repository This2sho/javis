package com.javis.learn_hub.support.presentation;

import com.javis.learn_hub.category.service.CategoryService;
import com.javis.learn_hub.category.service.dto.AllCategoryNodesResponse;
import com.javis.learn_hub.problem.domain.Difficulty;
import com.javis.learn_hub.support.domain.Authenticated;
import com.javis.learn_hub.support.domain.MemberId;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Slf4j
@RequiredArgsConstructor
@Controller
public class PageController {

    private final CategoryService categoryService;

    @GetMapping("/")
    public String mainPage(
            @Authenticated(required = false) MemberId memberId,
            Model model
            ) {
        model.addAttribute("memberId", memberId.getId());
        return "main";
    }

    @GetMapping("/mypage")
    public String myPage(Model model) {
        List<Map<String, Object>> categories = List.of(
                Map.of("name", "실제 인터뷰", "score", 80),
                Map.of("name", "기술 인터뷰", "score", 75),
                Map.of("name", "인성 인터뷰", "score", 85),
                Map.of("name", "CS 인터뷰", "score", 90),
                Map.of("name", "서버 기술 인터뷰", "score", 70)
        );
        model.addAttribute("categories", categories);
        return "mypage";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/interviews")
    public String interviewListPage() {
        return "interviews";
    }

    @GetMapping("/interviews/{interviewId}")
    public String interviewDetailPage(
            @PathVariable Long interviewId,
            Model model
    ) {
        model.addAttribute("interviewId", interviewId);
        return "interview-detail";
    }

    @GetMapping("/interviews/start/{mainCategory}")
    public String interviewPage(
            @PathVariable String mainCategory,
            Model model
    ) {

        model.addAttribute("interviewTitle", mainCategory);
        model.addAttribute(
                "welcomeMessage",
                String.format("안녕하세요! %s 인터뷰를 진행합니다.", mainCategory)
        );
        model.addAttribute("mainCategory", mainCategory);
        return "interview";
    }

    @GetMapping("/scores/{mainCategory}")
    public String scoreDetailPage(
            @PathVariable String mainCategory
    ) {
        return "score-detail";
    }

    @GetMapping("/problems/new")
    public String problemCreatePage(Model model) {
        AllCategoryNodesResponse allCategories = categoryService.getAllCategories();
        model.addAttribute("categoryTree", allCategories);
        model.addAttribute("difficulties", Difficulty.values());
        return "problem-create";
    }

    @GetMapping("/problems")
    public String problemListPage() {
        return "problems";
    }

    @GetMapping("/problems/{problemId}")
    public String problemDetailPage(
            @PathVariable Long problemId,
            Model model
    ) {
        AllCategoryNodesResponse allCategories = categoryService.getAllCategories();
        model.addAttribute("problemId", problemId);
        model.addAttribute("categoryTree", allCategories);
        model.addAttribute("difficulties", Difficulty.values());
        return "problem-detail";
    }

    @GetMapping("/review-requests")
    public String reviewListPage() {
        return "review-requests";
    }
}

