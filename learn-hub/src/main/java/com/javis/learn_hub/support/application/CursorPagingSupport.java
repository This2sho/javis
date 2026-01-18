package com.javis.learn_hub.support.application;

import com.javis.learn_hub.support.application.dto.CursorPage;
import com.javis.learn_hub.support.application.dto.CursorPageRequest;
import com.javis.learn_hub.support.application.dto.CursorResponse;
import com.javis.learn_hub.support.domain.BaseEntity;
import java.util.ArrayList;
import java.util.List;

public final class CursorPagingSupport {

    private CursorPagingSupport() {
    }

    public static <T extends BaseEntity> CursorPage<T> slice(List<T> items, CursorPageRequest request) {
        boolean hasNext = items.size() > request.getPageSize();
        CursorResponse nextCursor = makeNextCursor(hasNext, items);
        int itemSize = Math.min(request.getPageSize(),  items.size());
        items = new ArrayList<>(items.subList(0, itemSize));
        return new CursorPage<T>(items, nextCursor, hasNext);
    }

    private static <T extends BaseEntity> CursorResponse makeNextCursor(boolean hasNext, List<T> items) {
        if (hasNext) {
            T last = items.get(items.size() - 1);
            return new CursorResponse(last.getUpdatedAt(), last.getId());
        }
        return CursorResponse.EMPTY;
    }
}
