package org.qualifaizebackendapi.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.qualifaizebackendapi.DTO.db_object.SubsectionRow;
import org.qualifaizebackendapi.DTO.parseDTO.SubsectionWithContent;
import org.qualifaizebackendapi.DTO.response.pdf.table_of_contents_response.SubsectionDetailsDTO;
import org.qualifaizebackendapi.model.Document;
import org.qualifaizebackendapi.model.Subsection;

@Mapper(componentModel = "spring")
public interface SubsectionMapper {

    @Mapping(target = "title", source = "subsectionRow.title")
    @Mapping(target = "level", source = "subsectionRow.level")
    @Mapping(target = "order", source = "subsectionRow.position")
    @Mapping(target = "subsections", ignore = true)
    SubsectionDetailsDTO toSubsectionDetailsDTO(SubsectionRow subsectionRow);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "document", source = "document")
    @Mapping(target = "parent", source = "parent")
    @Mapping(target = "children", ignore = true)
    @Mapping(target = "title", source = "subsection.title")
    @Mapping(target = "content", source = "subsection.content")
    @Mapping(target = "position", source = "position")
    @Mapping(target = "level", source = "level")
    @Mapping(target = "createdAt", ignore = true)
    Subsection mapToSubsection(SubsectionWithContent subsection, Subsection parent, Document document, int level, int position);
}
