package com.javis.learn_hub.support.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
public abstract class CreatedOnlyEntity {

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public abstract Long getId();
}
