package com.javis.learn_hub.support.application.dto;

import java.util.List;

public record CursorPage<T>(
        List<T> content,
        CursorResponse nextCursor,
        boolean hasNext
) {

}
