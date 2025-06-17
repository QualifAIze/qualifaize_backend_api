package org.qualifaizebackendapi.mapper;

import org.mapstruct.*;
import org.qualifaizebackendapi.DTO.db_object.DocumentWithUserRow;
import org.qualifaizebackendapi.DTO.parseDTO.ParsedDocumentDetailsResponse;
import org.qualifaizebackendapi.DTO.response.pdf.UploadedPdfResponse;
import org.qualifaizebackendapi.DTO.response.pdf.table_of_contents_response.SubsectionDetailsDTO;
import org.qualifaizebackendapi.DTO.response.pdf.table_of_contents_response.UploadedPdfResponseWithToc;
import org.qualifaizebackendapi.model.Document;

import java.util.List;

@Mapper(componentModel = "spring", uses = UserMapper.class)
public interface PdfMapper {

    @Named("toUploadedPdfResponse")
    @Mapping(target = "uploadedBy", source = "dbData", qualifiedByName = "toUserOverviewResponseFromDocumentWithUserRow")
    UploadedPdfResponse toUploadedPdfResponse(DocumentWithUserRow dbData);

    @IterableMapping(qualifiedByName = "toUploadedPdfResponse")
    List<UploadedPdfResponse> toUploadedPdfResponses(List<DocumentWithUserRow> dbData);

    @InheritConfiguration(name = "toUploadedPdfResponse")
    UploadedPdfResponseWithToc toUploadedPdfResponseWithToc(DocumentWithUserRow dbData);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "subsections", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "subsectionsCount", source = "parsedResponse", qualifiedByName = "extractSubsectionsCount")
    @Mapping(target = "fileName", source = "parsedResponse.originalFilename")
    @Mapping(target = "secondaryFileName", source = "secondaryFilename")
    Document toDocument(ParsedDocumentDetailsResponse parsedResponse, String secondaryFilename);

    @Named("extractSubsectionsCount")
    default int extractSubsectionsCount(ParsedDocumentDetailsResponse documentDetails) {
        return documentDetails.getTocWithContent().getSubsections() != null
                ? documentDetails.getTocWithContent().getSubsections().size()
                : 0;
    }


}
