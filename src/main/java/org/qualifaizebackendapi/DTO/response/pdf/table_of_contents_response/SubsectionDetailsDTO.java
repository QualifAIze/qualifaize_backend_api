package org.qualifaizebackendapi.DTO.response.pdf.table_of_contents_response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SubsectionDetailsDTO {
    private String title;
    private int level;
    private int order;
    private List<SubsectionDetailsDTO> subsections;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("Section titled '").append(title)
                .append("' at level ").append(level)
                .append(" in position ").append(order);

        if (subsections == null || subsections.isEmpty()) {
            sb.append(" with no subsections");
        } else {
            sb.append(" containing ").append(subsections.size())
                    .append(" subsection").append(subsections.size() > 1 ? "s" : "")
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
