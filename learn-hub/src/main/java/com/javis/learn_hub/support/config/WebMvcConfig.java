package com.javis.learn_hub.support.config;

import com.javis.learn_hub.support.config.argumentresolver.AuthArgumentResolver;
import com.javis.learn_hub.support.config.argumentresolver.CursorPageRequestArgumentResolver;
import com.javis.learn_hub.support.config.interceptor.AuthenticationInterceptor;
import com.javis.learn_hub.support.config.interceptor.AuthorizationInterceptor;
import com.javis.learn_hub.support.perf.ApiTimingInterceptor;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@RequiredArgsConstructor
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthenticationInterceptor authenticationInterceptor;
    private final AuthorizationInterceptor authorizationInterceptor;
    private final AuthArgumentResolver authArgumentResolver;
    private final CursorPageRequestArgumentResolver cursorPageRequestArgumentResolver;
    private final ApiTimingInterceptor apiTimingInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authenticationInterceptor)
                .order(0)
                .addPathPatterns("/**")
                .excludePathPatterns(List.of(
                        "/",
                        "/kakao/**",
                        "/login/**",
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/fonts/**",
                        "/webjars/**",
                        "/favicon.ico",
                        "/error",
                        "/test"
                ));

        registry.addInterceptor(authorizationInterceptor)
                .order(10)
                .addPathPatterns("/**")
                .excludePathPatterns(List.of(
                        "/",
                        "/kakao/**",
                        "/login/**",
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/fonts/**",
                        "/webjars/**",
                        "/favicon.ico",
                        "/error",
                        "/test"
                ));

        registry.addInterceptor(apiTimingInterceptor);
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(authArgumentResolver);
        resolvers.add(cursorPageRequestArgumentResolver);
    }
}
