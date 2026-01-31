package com.javis.learn_hub.interview.domain;

public class EmptyProblemException extends RuntimeException {

    public EmptyProblemException() {
        super("인터뷰에 사용할 문제가 존재하지 않습니다. 문제를 먼저 작성해주세요.");
    }
}
