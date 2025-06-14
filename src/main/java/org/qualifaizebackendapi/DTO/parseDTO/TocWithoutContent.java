package org.qualifaizebackendapi.DTO.parseDTO;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TocWithoutContent extends Toc {
    private List<Subsection> subsections;
}
