package com.javis.learn_hub.support.application.dto;

import com.javis.learn_hub.support.domain.UserProfile;

public record KakaoProfileResponse(
        Long id
) implements UserProfile {

    @Override
    public Long getSocialId() {
        return id;
    }
}
