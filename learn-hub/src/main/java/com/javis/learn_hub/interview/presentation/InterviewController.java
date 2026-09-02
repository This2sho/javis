package com.javis.learn_hub.interview.presentation;

import com.javis.learn_hub.category.domain.MainCategory;
import com.javis.learn_hub.interview.service.InterviewFlowService;
import com.javis.learn_hub.interview.service.dto.QuestionResponse;
import com.javis.learn_hub.support.domain.Authenticated;
import com.javis.learn_hub.support.domain.MemberId;
import com.javis.learn_hub.support.i18n.ContentLanguage;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api")
@RestController
public class InterviewController {

    private final InterviewFlowService interviewFlowService;

    @PostMapping("/interviews/start/{mainCategory}")
    public ResponseEntity<QuestionResponse> startInterview(
            @PathVariable String mainCategory,
            Locale locale,
            @Authenticated MemberId memberId
    ) {
        MainCategory resolvedMainCategory = MainCategory.from(mainCategory);
        QuestionResponse questionResponse = interviewFlowService.start(
                resolvedMainCategory.getPath(),
                memberId.getId(),
                resolvedMainCategory.resolveContentLanguage(ContentLanguage.from(locale))
        );
        return ResponseEntity.ok(questionResponse);
    }
}
