package org.qualifaizebackendapi.DTO.response.pdf;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.qualifaizebackendapi.DTO.response.user.UserDetailsOverviewResponse;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UploadedPdfResponse {
    private UUID id;
    private String filename;
    private String secondaryFilename;
    private OffsetDateTime createdAt;
    private UserDetailsOverviewResponse uploadedBy;

    public UploadedPdfResponse(UUID id, String filename, String secondaryFilename, OffsetDateTime createdAt) {
        this.id = id;
        this.filename = filename;
        this.secondaryFilename = secondaryFilename;
        this.createdAt = createdAt;
    }
}
