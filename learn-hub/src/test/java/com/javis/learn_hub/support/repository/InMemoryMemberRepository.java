package com.javis.learn_hub.support.repository;

import com.javis.learn_hub.member.domain.Member;
import com.javis.learn_hub.member.domain.repository.MemberRepository;
import com.javis.learn_hub.support.domain.Provider;
import java.util.Optional;

public class InMemoryMemberRepository extends InMemoryRepository<Member> implements MemberRepository {

    @Override
    public Optional<Member> findByProviderAndSocialId(Provider provider, Long socialId) {
        return findOne(member -> member.getProvider().equals(provider) && member.getSocialId().equals(socialId));
    }
}
