package com.javis.learn_hub.problem.domain;

/**
 *     PRIVATE: 공식 문제 x
 *     PUBLIC: 공식 문제 채택 o
 *     INHERITED: visibility는 root Problem 에서만 사용되기 때문에 followUp Problem의 경우 INHERITED로 통일
 */
public enum Visibility {
    PRIVATE,
    PUBLIC,
    INHERITED;
}
