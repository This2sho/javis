package com.javis.learn_hub.support.application.dto;

import org.springframework.data.domain.Sort;

public enum CursorSortDirection {
    ASC(Sort.by(Sort.Direction.ASC, "updatedAt", "id")),
    DESC(Sort.by(Sort.Direction.DESC, "updatedAt", "id"));

    private Sort sort;

    CursorSortDirection(Sort sort) {
        this.sort = sort;
    }

    public Sort getSort() {
        return sort;
    }
}
