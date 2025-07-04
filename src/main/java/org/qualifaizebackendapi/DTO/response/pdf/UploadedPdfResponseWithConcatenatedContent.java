package org.qualifaizebackendapi.DTO.response.pdf;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UploadedPdfResponseWithConcatenatedContent{
    private String title;
    private String content;
}
