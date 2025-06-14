package org.qualifaizebackendapi.DTO.parseDTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SubsectionWithoutContent {
    private String title;

    @JsonProperty("subsections_count")
    private int subsectionsCount;

    private List<SubsectionWithContent> subsections;
}
