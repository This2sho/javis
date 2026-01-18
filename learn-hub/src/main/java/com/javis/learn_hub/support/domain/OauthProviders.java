package com.javis.learn_hub.support.domain;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class OauthProviders {

    private final List<OauthProvider> providers;

    public OauthProvider mapping(String providerName) {
        return providers.stream()
                .filter(provider -> provider.is(providerName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 provider 입니다."));
    }
}
