package com.javis.learn_hub.support.perf;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class ApiTimingInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) {
        long start = System.currentTimeMillis();
        ApiContextHolder.set(new ApiContext(start));

        String requestId = UUID.randomUUID().toString().substring(0, 8);
        MDC.put("reqId", requestId);

        log.info("[API START] {} {}",
                request.getMethod(),
                request.getRequestURI()
        );
        return true;
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception ex
    ) {
        ApiContext ctx = ApiContextHolder.get();
        long total = ctx.totalTime();

        log.info("[API END] {} {} ({} ms)",
                request.getMethod(),
                request.getRequestURI(),
                total
        );

        // 메서드별 리포트
        for (MethodRecord r : ctx.getRecords()) {
            double percent = r.time() * 100.0 / total;
            log.info(" - {} ({} ms, {}%)",
                    r.method(),
                    r.time(),
                    String.format("%.1f", percent)
            );
        }

        ApiContextHolder.clear();
        MDC.clear();
    }
}
