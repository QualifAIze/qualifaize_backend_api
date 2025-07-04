package org.qualifaizebackendapi.mapper;

import org.mapstruct.*;
import org.qualifaizebackendapi.DTO.response.pdf.UploadedPdfResponse;
import org.qualifaizebackendapi.model.Document;

import java.util.List;

@Mapper(componentModel = "spring", uses = UserMapper.class)
public interface PdfMapper {

    @Mapping(target = "filename", source = "document.fileName")
    @Mapping(target = "secondaryFilename", source = "document.secondaryFileName")
    @Mapping(target = "uploadedBy", source = "document.uploadedByUser", qualifiedByName = "toUserDetailsOverviewResponse")
    @Named("toUploadedPdfResponse")
    UploadedPdfResponse toUploadedPdfResponse(Document document);

    @IterableMapping(qualifiedByName = "toUploadedPdfResponse")
    List<UploadedPdfResponse> toUploadedPdfResponseList(List<Document> documents);

}
