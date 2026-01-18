package com.javis.learn_hub.interview.domain.service.dto;

import java.util.Set;

public record ReferenceView(
        String referenceAnswer,
        Set<String> keywords
) {

}
