package org.qualifaizebackendapi.DTO.response.pdf.table_of_contents_response;

import lombok.Getter;
import lombok.Setter;
import org.qualifaizebackendapi.DTO.response.pdf.UploadedPdfResponse;

import java.util.List;

@Getter
@Setter
public class UploadedPdfResponseWithToc extends UploadedPdfResponse {
    private List<SubsectionDetailsDTO> subsections;
}
