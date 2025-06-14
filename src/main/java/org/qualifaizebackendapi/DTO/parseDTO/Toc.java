package org.qualifaizebackendapi.DTO.parseDTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class Toc {
    private String title;
    @JsonProperty("subsections_count")
    private int subsectionsCount;
}
