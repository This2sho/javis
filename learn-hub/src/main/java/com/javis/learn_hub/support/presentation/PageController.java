package com.javis.learn_hub.support.presentation;

import com.javis.learn_hub.category.service.CategoryService;
import com.javis.learn_hub.category.service.dto.AllCategoryNodesResponse;
import com.javis.learn_hub.problem.domain.Difficulty;
import com.javis.learn_hub.support.domain.Authenticated;
import com.javis.learn_hub.support.domain.MemberId;
import com.javis.learn_hub.support.i18n.MainCategoryLabelResolver;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Slf4j
@RequiredArgsConstructor
@Controller
public class PageController {

    private final CategoryService categoryService;
    private final MainCategoryLabelResolver mainCategoryLabelResolver;
    private final MessageSource messageSource;

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
            Model model,
            Locale locale
    ) {
        String mainCategoryLabel = mainCategoryLabelResolver.resolveByPath(mainCategory, locale);
        String interviewTitle = messageSource.getMessage("interview.title", new Object[]{mainCategoryLabel}, locale);
        String welcomeMessage = messageSource.getMessage("interview.welcome", new Object[]{mainCategoryLabel}, locale);

        model.addAttribute("interviewTitle", interviewTitle);
        model.addAttribute("welcomeMessage", welcomeMessage);
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
