package org.qualifaizebackendapi.DTO.response.pdf;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UploadedPdfResponse {
    private Long id;
    private String fileName;
    private String secondaryFileName;
    private OffsetDateTime createdAt;
}
