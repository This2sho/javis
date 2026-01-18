package com.javis.learn_hub.support.config.argumentresolver;

import com.javis.learn_hub.support.application.dto.CursorPageRequest;
import com.javis.learn_hub.support.presentation.WithCursor;
import java.time.LocalDateTime;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class CursorPageRequestArgumentResolver
        implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().equals(CursorPageRequest.class)
                && parameter.hasParameterAnnotation(WithCursor.class);
    }

    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory
    ) {

        WithCursor withCursor = parameter.getParameterAnnotation(WithCursor.class);

        int defaultSize = withCursor.size();

        String sizeParam = webRequest.getParameter("size");
        String targetTimeParam = webRequest.getParameter("targetUpdatedAt");
        String targetIdParam = webRequest.getParameter("targetId");

        CursorPageRequest.CursorPageRequestBuilder builder = CursorPageRequest.builder();

        // size
        int size = sizeParam != null
                ? Integer.parseInt(sizeParam)
                : defaultSize;
        builder.withPageSize(size);

        // cursor
        if (targetTimeParam != null && targetIdParam != null) {
            builder.withTargetTime(LocalDateTime.parse(targetTimeParam));
            builder.withTargetId(Long.parseLong(targetIdParam));
        }

        return builder.build();
    }
}

