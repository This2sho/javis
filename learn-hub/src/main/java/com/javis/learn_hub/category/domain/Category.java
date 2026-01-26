package com.javis.learn_hub.category.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.StringJoiner;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@EqualsAndHashCode(of = {"id"})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_category_path",
                        columnNames = "path"
                )
        }
)
public class Category {

    private static final String DELIMITER = ":";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String path;

    private Category(String path) {
        this.path = path;
    }
    
    public static Category of(MainCategory mainCategory, String... subCategories) {
        if (subCategories == null || subCategories.length == 0) {
            return new Category(mainCategory.getPath());
        }
        return new Category(makePath(mainCategory, subCategories));
    }

    private static String makePath(MainCategory mainCategory, String... subCategories) {
        StringJoiner joiner = new StringJoiner(DELIMITER);
        joiner.add(mainCategory.getPath());
        for (String subCategory : subCategories) {
            joiner.add(subCategory.toLowerCase());
        }
        return joiner.toString();
    }

    public MainCategory getMainCategory() {
        String[] paths = path.split(DELIMITER);
        return MainCategory.from(paths[0]);
    }

    public static String getDelimiter() {
        return DELIMITER;
    }
}
