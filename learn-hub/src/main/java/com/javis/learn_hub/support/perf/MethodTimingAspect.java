package com.javis.learn_hub.support.perf;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class MethodTimingAspect {

    @Around("""
        execution(* com.javis.learn_hub..*(..))
            && !execution(* com.javis.learn_hub.support..*(..))
            && !execution(* com.javis.learn_hub..dto..*(..))
    """)
    public Object trace(ProceedingJoinPoint joinPoint) throws Throwable {

        long start = System.currentTimeMillis();
        try {
            return joinPoint.proceed();
        } finally {
            long time = System.currentTimeMillis() - start;

            ApiContext ctx = ApiContextHolder.get();
            if (ctx != null) {
                ctx.add(
                        joinPoint.getSignature().toShortString(),
                        time
                );
            }
        }
    }
}
