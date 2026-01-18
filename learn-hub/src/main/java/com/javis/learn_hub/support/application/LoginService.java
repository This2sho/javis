package com.javis.learn_hub.support.application;

import com.javis.learn_hub.member.domain.Member;
import com.javis.learn_hub.member.domain.repository.MemberRepository;
import com.javis.learn_hub.support.domain.OauthProvider;
import com.javis.learn_hub.support.domain.OauthProviders;
import com.javis.learn_hub.support.domain.Provider;
import com.javis.learn_hub.support.domain.UserProfile;
import com.javis.learn_hub.support.infrastructure.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class LoginService {

    private final MemberRepository memberRepository;
    private final OauthProviders oAuthProviders;
    private final JwtUtil jwtUtil;

    @Transactional
    public String signIn(String providerName, String accessToken) {
        OauthProvider provider = oAuthProviders.mapping(providerName);
        UserProfile userProfile = provider.getUserProfile(accessToken);
        Member member = memberRepository.findByProviderAndSocialId(Provider.find(providerName), userProfile.getSocialId())
                .orElseGet(() -> signUp(Provider.find(providerName), userProfile.getSocialId()));
        return jwtUtil.generateToken(member.getId(), member.getRole());
    }

    private Member signUp(Provider provider, Long socialId) {
        Member member = new Member(provider, socialId);
        memberRepository.save(member);
        return member;
    }
}
