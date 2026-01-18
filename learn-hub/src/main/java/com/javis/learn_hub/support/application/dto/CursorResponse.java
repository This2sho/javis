package com.javis.learn_hub.support.application.dto;

import java.time.LocalDateTime;

public record CursorResponse(
        LocalDateTime targetTime,
        Long targetId
) {
    public static CursorResponse EMPTY = new CursorResponse(null, -1L);
}
