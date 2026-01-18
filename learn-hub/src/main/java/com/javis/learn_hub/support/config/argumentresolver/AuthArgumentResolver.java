package com.javis.learn_hub.support.config.argumentresolver;

import com.javis.learn_hub.support.domain.Authenticated;
import com.javis.learn_hub.support.domain.MemberId;
import com.javis.learn_hub.support.infrastructure.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@RequiredArgsConstructor
@Component
public class AuthArgumentResolver implements HandlerMethodArgumentResolver {

    private final JwtUtil jwtUtil;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().equals(MemberId.class) &&
                parameter.hasParameterAnnotation(Authenticated.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        Long id = resolveMemberId((HttpServletRequest) webRequest.getNativeRequest());
        Authenticated authenticated = parameter.getParameterAnnotation(Authenticated.class);
        if (!authenticated.required() && id == null) {
            return MemberId.guest();
        }

        return MemberId.from(id);
    }

    private Long resolveMemberId(HttpServletRequest request) {
        try {
            String accessToken = jwtUtil.resolveToken(request);
            return jwtUtil.getMemberId(accessToken);
        } catch (Exception e) {
            return null;
        }
    }
}
