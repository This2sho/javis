package com.javis.learn_hub.review.domain;

import com.javis.learn_hub.member.domain.Member;
import com.javis.learn_hub.problem.domain.Problem;
import com.javis.learn_hub.support.domain.Association;
import com.javis.learn_hub.support.domain.BaseEntity;
import com.javis.learn_hub.support.infrastructure.AssociationConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_review_root_problem_id",
                        columnNames = {"root_problem_id"}
                )
        }
)
@Entity
public class Review extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Convert(converter = AssociationConverter.class)
    private Association<Problem> rootProblemId;

    @Column(nullable = false)
    @Convert(converter = AssociationConverter.class)
    private Association<Member> revieweeId;

    @Enumerated(EnumType.STRING)
    private RegistrationStatus registrationStatus;

    public Review(Association<Problem> rootProblemId, Association<Member> revieweeId, RegistrationStatus registrationStatus) {
        this.rootProblemId = rootProblemId;
        this.revieweeId = revieweeId;
        this.registrationStatus = registrationStatus;
    }

    public void update(RegistrationStatus status) {
        this.registrationStatus = status;
    }
}
