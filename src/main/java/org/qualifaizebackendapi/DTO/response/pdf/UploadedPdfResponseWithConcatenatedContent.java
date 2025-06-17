package org.qualifaizebackendapi.DTO.response.pdf;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class UploadedPdfResponseWithConcatenatedContent extends UploadedPdfResponse{
    private String title;
    private String content;
    Long subsectionsCount;

    public UploadedPdfResponseWithConcatenatedContent(UUID id, String filename, String secondaryFilename, OffsetDateTime createdAt, String title, String content, Long subsectionsCount) {
        super(id, filename, secondaryFilename, createdAt);
        this.title = title;
        this.content = content;
        this.subsectionsCount = subsectionsCount;
    }
}
