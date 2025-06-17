package org.qualifaizebackendapi.DTO.db_object;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class DocumentWithUserRow extends UserDetailsOverviewRow {
    private UUID id;
    private String filename;
    private String secondaryFilename;
    private OffsetDateTime createdAt;

    public DocumentWithUserRow(UUID userId, String username, String firstName, String lastName, UUID id, String filename, String secondaryFilename, OffsetDateTime createdAt) {
        super(userId, username, firstName, lastName);
        this.id = id;
        this.filename = filename;
        this.secondaryFilename = secondaryFilename;
        this.createdAt = createdAt;
    }
}
