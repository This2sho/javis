package com.javis.learn_hub.support.application.dto;

import java.time.LocalDateTime;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class CursorPageRequest {

    private static final Sort DEFAULT_SORT = CursorSortDirection.DESC.getSort();
    private static final int DEFAULT_PAGE_NUMBER = 0;
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 20;

    private Pageable pageable;
    private Cursor cursor;

    private CursorPageRequest(Pageable pageable, Cursor cursor) {
        this.pageable = pageable;
        this.cursor = cursor;
    }

    public Pageable getPageable() {
        return pageable;
    }

    public int getPageSize() {
        return pageable.getPageSize() - 1;
    }

    public LocalDateTime getTargetTime() {
        return cursor.targetTime;
    }

    public Long getTargetId() {
        return cursor.targetId;
    }

    public boolean isDesc() {
        return this.pageable.getSort() == CursorSortDirection.DESC.getSort();
    }

    public static CursorPageRequestBuilder builder() {
        return new CursorPageRequestBuilder();
    }

    private record Cursor(LocalDateTime targetTime, Long targetId) {
        private static Cursor LATEST = new Cursor(LocalDateTime.of(3000, 12, 31, 23, 59, 59), Long.MAX_VALUE);
        private static Cursor OLDEST = new Cursor(LocalDateTime.of(2000, 1, 1, 0,0,0), Long.MIN_VALUE);
    }

    public static class CursorPageRequestBuilder {

        private int pageSize = DEFAULT_PAGE_SIZE;
        private Sort sort = DEFAULT_SORT;
        private LocalDateTime targetTime = null;
        private Long targetId = null;

        public CursorPageRequestBuilder withPageSize(int pageSize) {
            this.pageSize = Math.min(pageSize, MAX_PAGE_SIZE);
            return this;
        }

        public CursorPageRequestBuilder withSort(CursorSortDirection sortDirection) {
            this.sort = sortDirection.getSort();
            return this;
        }

        public CursorPageRequestBuilder withTargetTime(LocalDateTime targetTime) {
            this.targetTime = targetTime;
            return this;
        }

        public CursorPageRequestBuilder withTargetId(Long targetId) {
            this.targetId = targetId;
            return this;
        }

        public CursorPageRequest build() {
            PageRequest pageRequest = PageRequest.of(DEFAULT_PAGE_NUMBER, pageSize + 1, sort);
            if (targetTime == null || targetId == null) {
                if (sort == CursorSortDirection.DESC.getSort()) {
                    return new CursorPageRequest(pageRequest, Cursor.LATEST);
                }
                if (sort == CursorSortDirection.ASC.getSort()) {
                    return new CursorPageRequest(pageRequest, Cursor.OLDEST);
                }
            }
            Cursor cursor = new Cursor(targetTime, targetId);
            return new CursorPageRequest(pageRequest, cursor);
        }
    }
}
