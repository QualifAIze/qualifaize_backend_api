package org.qualifaizebackendapi.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.qualifaizebackendapi.DTO.parseDTO.ParsedDocumentDetailsResponse;
import org.qualifaizebackendapi.DTO.parseDTO.SubsectionWithContent;
import org.qualifaizebackendapi.DTO.response.pdf.UploadedPdfResponse;
import org.qualifaizebackendapi.model.Document;
import org.qualifaizebackendapi.model.Subsection;

@Mapper(componentModel = "spring")
public interface PdfMapper {

    @Mapping(target = "fileName", source = "parsedResponse.fileName")
    @Mapping(target = "secondaryFileName", source = "secondaryFileName")
    @Mapping(target = "createdAt", source = "parsedResponse.createdAt")
    @Mapping(target = "id", source = "parsedResponse.id")
    UploadedPdfResponse toUploadedPdfResponse(Document parsedResponse, String secondaryFileName);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "subsections", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "subsectionsCount", source = "parsedResponse", qualifiedByName = "extractSubsectionsCount")
    @Mapping(target = "fileName", source = "parsedResponse.originalFileName")
    @Mapping(target = "secondaryFileName", source = "secondaryFileName")
    Document toDocument(ParsedDocumentDetailsResponse parsedResponse, String secondaryFileName);

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

    @Named("extractSubsectionsCount")
    default int extractSubsectionsCount(ParsedDocumentDetailsResponse documentDetails) {
        return documentDetails.getTocWithContent().getSubsections() != null
                ? documentDetails.getTocWithContent().getSubsections().size()
                : 0;
    }


}
