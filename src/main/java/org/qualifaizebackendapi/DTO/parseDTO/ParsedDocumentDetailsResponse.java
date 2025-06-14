package org.qualifaizebackendapi.DTO.parseDTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ParsedDocumentDetailsResponse {
    @JsonProperty("original_file_name")
    private String originalFileName;

    private TocWithoutContent toc;

    @JsonProperty("toc_with_content")
    private TocWithContent tocWithContent;
}
