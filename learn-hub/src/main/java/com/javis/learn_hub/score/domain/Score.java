package com.javis.learn_hub.score.domain;

import com.javis.learn_hub.category.domain.Category;
import com.javis.learn_hub.member.domain.Member;
import com.javis.learn_hub.support.domain.Association;
import com.javis.learn_hub.support.infrastructure.AssociationConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
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
                        name = "uk_score_member_id_category_id",
                        columnNames = {"member_id", "category_id"}
                )
        }
)
@Entity
public class Score implements Comparable<Score> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Convert(converter = AssociationConverter.class)
    private Association<Member> memberId;

    @Column(nullable = false)
    @Convert(converter = AssociationConverter.class)
    private Association<Category> categoryId;

    private int score;

    public Score(Association<Member> memberId, Association<Category> categoryId) {
        this.memberId = memberId;
        this.categoryId = categoryId;
        this.score = 0;
    }

    @Override
    public int compareTo(Score o) {
        return Integer.compare(this.score, o.score);
    }

    public void addScore(int score) {
        this.score += score;
    }
}
