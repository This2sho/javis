package com.javis.learn_hub.support.presentation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface WithCursor {

    /** 페이지 사이즈 기본값 */
    int size() default 10;

    /** targetUpdatedAt 파라미터를 사용할지 */
    boolean requiredTargetTime() default true;

    boolean requiredTargetId() default true;
}

