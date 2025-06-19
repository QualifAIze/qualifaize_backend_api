package org.qualifaizebackendapi.DTO.response.pdf.table_of_contents_response;

import lombok.Getter;
import lombok.Setter;
import org.qualifaizebackendapi.DTO.response.pdf.UploadedPdfResponse;

import java.util.List;

@Getter
@Setter
public class UploadedPdfResponseWithToc extends UploadedPdfResponse {
    private List<SubsectionDetailsDTO> subsections;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(super.toString()).append(" ");

        if (subsections == null || subsections.isEmpty()) {
            sb.append("Document contains no sections");
        } else {
            sb.append("Document contains ").append(subsections.size())
                    .append(" root section").append(subsections.size() > 1 ? "s" : "")
                    .append(" which are: ");

            for (int i = 0; i < subsections.size(); i++) {
                if (i > 0) {
                    sb.append(" and ");
                }
                sb.append(subsections.get(i).toString());
            }
        }

        return sb.toString();
    }
}
