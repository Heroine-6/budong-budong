package com.example.budongbudong.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @CreatedDate
    @Column(name="created_at",nullable = false,updatable = false)
    protected LocalDateTime createdAt;

    @Column(name="is_deleted", nullable = false)
    private Boolean isDeleted=false;

    public void softDelete() {
        this.isDeleted = true;
    }
}
