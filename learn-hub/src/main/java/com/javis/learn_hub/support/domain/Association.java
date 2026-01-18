package com.javis.learn_hub.support.domain;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = {"id"})
@Embeddable
public class Association<T> {

    private static Association EMPTY = new Association(-1L);

    private Long id;

    private Association(Long id) {
        this.id = id;
    }

    public static <T> Association<T> from(Long id) {
        if (id == null || id == -1) {
            return EMPTY;
        }
        return new Association<>(id);
    }

    public static Association getEmpty() {return EMPTY;}

    public Long getId() {
        return id;
    }

    public boolean isEmpty() {
        return this == EMPTY;
    }
}
