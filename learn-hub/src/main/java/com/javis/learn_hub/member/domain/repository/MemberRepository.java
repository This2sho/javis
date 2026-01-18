package com.javis.learn_hub.member.domain.repository;

import com.javis.learn_hub.member.domain.Member;
import com.javis.learn_hub.support.domain.Provider;
import java.util.Optional;
import org.springframework.data.repository.Repository;

public interface MemberRepository extends Repository<Member, Long> {

    Member save(Member member);

    Optional<Member> findByProviderAndSocialId(Provider provider, Long socialId);

    Optional<Member> findById(Long id);
}
