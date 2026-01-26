package com.javis.learn_hub.problem.domain;

import com.javis.learn_hub.category.domain.Category;
import com.javis.learn_hub.member.domain.Member;
import com.javis.learn_hub.support.domain.Association;
import com.javis.learn_hub.support.domain.BaseEntity;
import com.javis.learn_hub.support.infrastructure.AssociationConverter;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = {"id"}, callSuper = false)
@Entity
@Table(
        indexes = {
                @Index(
                        name = "idx_problem_writer_id_parent_problem_id_updated_at_id",
                        columnList = "writer_id, parent_problem_id, updated_at, id"
                ),
                @Index(
                        name = "idx_problem_parent_problem_id",
                        columnList = "parent_problem_id"
                )
        }
)
public class Problem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Convert(converter = AssociationConverter.class)
    private Association<Category> categoryId;

    @Convert(converter = AssociationConverter.class)
    private Association<Problem> parentProblemId;

    @Convert(converter = AssociationConverter.class)
    private Association<Member> writerId;

    @Enumerated(EnumType.STRING)
    private Difficulty difficulty;

    @Enumerated(EnumType.STRING)
    private Visibility visibility;

    @Lob
    private String content;

    public Problem(Association<Category> categoryId, Association<Problem> parentProblemId, Association<Member> writerId,
                   Difficulty difficulty, String content, Visibility visibility) {
        this.categoryId = categoryId;
        this.parentProblemId = parentProblemId;
        this.writerId = writerId;
        this.difficulty = difficulty;
        this.content = content;
        this.visibility = visibility;
    }

    public void validateWriter(Association<Member> writerId) {
        if (this.writerId.equals(writerId)) {
            return;
        }
        throw new IllegalArgumentException("해당 문제를 작성한 작성자가 아닙니다.");
    }

    public void update(Association<Category> categoryId, Difficulty difficulty, String content) {
        if (!isUpdated(categoryId, difficulty, content)) {
            return;
        }
        this.categoryId = categoryId;
        this.difficulty = difficulty;
        this.content = content;
    }

    private boolean isUpdated(Association<Category> categoryId, Difficulty difficulty, String content) {
        if (categoryId.equals(this.categoryId) && difficulty.equals(this.difficulty)
                && content.equals(this.content)) {
            return false;
        }
        return true;
    }

    public void publish() {
        visibility = Visibility.PUBLIC;
    }

    public boolean isPublic() {
        return visibility == Visibility.PUBLIC;
    }

    public boolean isPrivate() {
        return visibility == Visibility.PRIVATE;
    }
}
