package com.javis.learn_hub.support.repository;

import com.javis.learn_hub.category.domain.MainCategory;
import com.javis.learn_hub.interview.domain.Interview;
import com.javis.learn_hub.interview.domain.InterviewStatus;
import com.javis.learn_hub.interview.domain.repository.InterviewRepository;
import com.javis.learn_hub.member.domain.Member;
import com.javis.learn_hub.support.domain.Association;
import com.javis.learn_hub.support.i18n.ContentLanguage;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import org.springframework.data.domain.Pageable;

public class InMemoryInterviewRepository extends InMemoryRepository<Interview> implements InterviewRepository {

    @Override
    public List<Interview> findAllByInterviewStatusAndMemberIdByLatest(InterviewStatus interviewStatus, LocalDateTime targetTime, Long targetId,
                                                                       Association<Member> memberId, Pageable pageable) {
        Predicate<Interview> cursorCondition = i ->
                i.getStatus().equals(interviewStatus) && i.getMemberId().equals(memberId)
                        && (i.getUpdatedAt().isBefore(targetTime)
                        || (i.getUpdatedAt().isEqual(targetTime) && i.getId() < targetId)
                );
        Comparator<Interview> latestOrder =
                Comparator.comparing(Interview::getUpdatedAt).reversed()
                        .thenComparing(Interview::getId, Comparator.reverseOrder());
        int pageSize = pageable.getPageSize();
        return findAll(cursorCondition)
                .stream()
                .sorted(latestOrder)
                .limit(pageSize)
                .toList();
    }

    @Override
    public List<Interview> findAllByInterviewStatusAndMemberIdByOldest(InterviewStatus interviewStatus, LocalDateTime targetTime, Long targetId,
                                                                       Association<Member> memberId, Pageable pageable) {
        Predicate<Interview> cursorCondition = i ->
                i.getStatus().equals(interviewStatus) && i.getMemberId().equals(memberId)
                        && (i.getUpdatedAt().isAfter(targetTime) ||
                (i.getUpdatedAt().isEqual(targetTime) && i.getId() > targetId)
        );
        Comparator<Interview> oldestOrder = Comparator.comparing(Interview::getUpdatedAt).thenComparing(Interview::getId);
        int pageSize = pageable.getPageSize();
        return findAll(cursorCondition)
                .stream()
                .sorted(oldestOrder)     // ORDER BY updatedAt ASC, id ASC
                .limit(pageSize)         // LIMIT size
                .toList();
    }

    @Override
    public Optional<Interview> findByMemberIdAndMainCategoryAndContentLanguageAndStatus(Association<Member> memberId,
                                                                                         MainCategory mainCategory,
                                                                                         ContentLanguage contentLanguage,
                                                                                         InterviewStatus interviewStatus) {
        return findOne(i -> i.getMemberId().equals(memberId)
            &&  i.getMainCategory().equals(mainCategory)
                && i.getContentLanguage() == contentLanguage
                && i.getStatus().equals(interviewStatus)
        );
    }

    @Override
    public List<Interview> findAllByMemberIdAndStatus(Association<Member> memberId, InterviewStatus interviewStatus) {
        return findAll(i -> i.getMemberId().equals(memberId)
                && i.getStatus().equals(interviewStatus));
    }

    @Override
    public long countByMemberIdAndMainCategoryAndContentLanguageAndCreatedAtBetween(Association<Member> memberId,
                                                                                    MainCategory mainCategory,
                                                                                    ContentLanguage contentLanguage,
                                                                                    LocalDateTime start,
                                                                                    LocalDateTime end) {
        return findAll(i -> i.getMemberId().equals(memberId)
                && i.getMainCategory().equals(mainCategory)
                && i.getContentLanguage() == contentLanguage
                && !i.getCreatedAt().isBefore(start)
                && i.getCreatedAt().isBefore(end)).size();
    }
}
