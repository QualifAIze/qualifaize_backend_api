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
}
