package com.javis.learn_hub.support.builder;

import com.javis.learn_hub.member.domain.Member;
import com.javis.learn_hub.support.domain.Provider;

public class MemberBuilder {

    private Provider provider = Provider.KAKAO;
    private Long socialId = 0L;

    public static MemberBuilder builder() {
        return new MemberBuilder();
    }

    public MemberBuilder withProvider(Provider provider) {
        this.provider = provider;
        return this;
    }

    public MemberBuilder withSocialId(Long socialId) {
        this.socialId = socialId;
        return this;
    }

    public Member build() {
        return new Member(provider, socialId);
    }
}

