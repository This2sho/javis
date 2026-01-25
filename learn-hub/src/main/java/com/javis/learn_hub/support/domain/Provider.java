package com.javis.learn_hub.support.domain;

import java.util.Arrays;

public enum Provider {
    KAKAO,
    TEST
    ;

    public static Provider find(String name) {
        return Arrays.stream(Provider.values())
                .filter(provider -> provider.name().equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 provider 입니다."));
    }
}
