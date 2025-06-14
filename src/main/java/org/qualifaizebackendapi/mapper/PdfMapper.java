package org.qualifaizebackendapi.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.qualifaizebackendapi.DTO.parseDTO.ParsedDocumentDetailsResponse;
import org.qualifaizebackendapi.DTO.response.pdf.UploadedPdfResponse;

@Mapper(componentModel = "spring")
public interface PdfMapper {

    @Mapping(target = "fileName", source = "parsedResponse.originalFileName")
    @Mapping(target = "secondaryFileName", source = "secondaryFileName")
    @Mapping(target = "createdAt", expression = "java(java.time.OffsetDateTime.now())")
    @Mapping(target = "id", constant = "1L")
    UploadedPdfResponse toUploadedPdfResponse(ParsedDocumentDetailsResponse parsedResponse, String secondaryFileName);
}
