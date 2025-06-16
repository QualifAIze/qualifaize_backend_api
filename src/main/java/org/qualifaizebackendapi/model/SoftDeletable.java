package org.qualifaizebackendapi.model;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@MappedSuperclass
public abstract class SoftDeletable {
    private boolean deleted = false;
    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

}