package com.javis.learn_hub.support.application.dto;

import java.util.List;

public record CursorPageResponse<T>(
        List<T> items,
        CursorResponse nextCursor,
        boolean hasNext
) {
}
