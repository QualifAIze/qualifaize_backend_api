package org.qualifaizebackendapi.DTO.parseDTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubsectionWithContent extends Subsection {
    private Integer page;
    private String content;
}
