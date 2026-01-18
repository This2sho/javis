package com.javis.learn_hub.problem.domain;

import jakarta.persistence.Embeddable;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Embeddable
public class Keywords {

    private static final String DELIMITER = ",";

    private Set<String> keywords;

    private Keywords(Set<String> keywords) {
        this.keywords = keywords;
    }

    public static Keywords from(Set<String> keywords) {
        validate(keywords);
        return new Keywords(keywords);
    }

    private static void validate(Set<String> keywords) {
        if (keywords == null || keywords.isEmpty()) {
            throw new IllegalArgumentException("키워드가 존재하지 않습니다.");
        }
        for (String keyword : keywords) {
            if (keyword.contains(DELIMITER)) {
                throw new IllegalArgumentException("키워드에는 , 문자를 포함할 수 없습니다.");
            }
        }
    }

    public Set<String> getKeywords() {
        return Collections.unmodifiableSet(keywords);
    }

    public static String getDelimiter() {
        return DELIMITER;
    }

    @Override
    public String toString() {
        return String.join(DELIMITER, keywords);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Keywords other)) return false;
        return Objects.equals(this.keywords, other.keywords);
    }

    @Override
    public int hashCode() {
        return Objects.hash(keywords);
    }
}
