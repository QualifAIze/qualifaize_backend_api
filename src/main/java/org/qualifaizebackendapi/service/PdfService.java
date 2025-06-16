package org.qualifaizebackendapi.service;

import org.qualifaizebackendapi.DTO.response.pdf.UploadedPdfResponse;
import org.qualifaizebackendapi.DTO.response.pdf.UploadedPdfResponseWithConcatenatedContent;
import org.qualifaizebackendapi.DTO.response.pdf.table_of_contents_response.UploadedPdfResponseWithToc;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public interface PdfService {
    UploadedPdfResponse savePdf(MultipartFile file, String secondaryFileName);
    UploadedPdfResponse changeDocumentSecondaryFilename(UUID documentId, String newTitle);
    UploadedPdfResponseWithToc getDocumentDetailsAndTocById(UUID documentId);
    UploadedPdfResponseWithConcatenatedContent getConcatenatedContentById(UUID documentId, String subsectionName);
    List<UploadedPdfResponse> getAllDocuments();
    void deleteDocument(UUID documentId);
}
