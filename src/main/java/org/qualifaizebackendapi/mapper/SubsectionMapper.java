package org.qualifaizebackendapi.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.qualifaizebackendapi.DTO.db_object.SubsectionRow;
import org.qualifaizebackendapi.DTO.response.pdf.table_of_contents_response.SubsectionDetailsDTO;

@Mapper(componentModel = "spring")
public interface SubsectionMapper {

    @Mapping(target = "title", source = "subsectionRow.title")
    @Mapping(target = "level", source = "subsectionRow.level")
    @Mapping(target = "order", source = "subsectionRow.position")
    @Mapping(target = "subsections", ignore = true)
    SubsectionDetailsDTO toSubsectionDetailsDTO(SubsectionRow subsectionRow);
}
