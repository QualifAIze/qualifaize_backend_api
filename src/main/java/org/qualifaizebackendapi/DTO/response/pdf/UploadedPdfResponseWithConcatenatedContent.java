package org.qualifaizebackendapi.DTO.response.pdf;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UploadedPdfResponseWithConcatenatedContent extends UploadedPdfResponse{
    private String title;
    private String content;
}
