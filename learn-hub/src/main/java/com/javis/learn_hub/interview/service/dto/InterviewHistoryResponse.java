package com.javis.learn_hub.interview.service.dto;

import com.javis.learn_hub.category.domain.Category;
import com.javis.learn_hub.category.domain.MainCategory;
import com.javis.learn_hub.interview.domain.Interview;
import java.time.LocalDateTime;

public record InterviewHistoryResponse(Long id, String categoryName, LocalDateTime endedAt) {

    public static InterviewHistoryResponse from(Interview interview, Category category) {
        return new InterviewHistoryResponse(interview.getId(), categoryFrom(category.getMainCategory()), interview.getUpdatedAt());
    }

    public static InterviewHistoryResponse from(Interview interview) {
        return new InterviewHistoryResponse(interview.getId(), categoryFrom(interview.getMainCategory()), interview.getUpdatedAt());
    }

    private static String categoryFrom(MainCategory mainCategory) {
        switch (mainCategory){
            case BACKEND -> {
                return "백엔드";
            }
            case SYSTEM_DESIGN -> {
                return "시스템 설계";
            }
            case COMPUTER_SCIENCE -> {
                return "컴퓨터 과학";
            }
            case CULTURE_FIT -> {
                return "컬처핏";
            }
            default -> {
                return "오류";
            }
        }
    }
}
