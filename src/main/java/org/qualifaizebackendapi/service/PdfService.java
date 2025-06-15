package org.qualifaizebackendapi.service;

import org.qualifaizebackendapi.DTO.response.pdf.UploadedPdfResponse;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public interface PdfService {
    Mono<UploadedPdfResponse> savePdf(MultipartFile file, String secondaryFileName);
    UploadedPdfResponse changeDocumentSecondaryFilename(UUID documentId, String newTitle);
    UploadedPdfResponse getDocumentById(UUID documentId);
    List<UploadedPdfResponse> getAllDocuments();
    void deleteDocument(UUID documentId);
}
