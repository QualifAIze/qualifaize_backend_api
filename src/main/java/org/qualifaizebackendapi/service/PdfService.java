package org.qualifaizebackendapi.service;

import org.qualifaizebackendapi.DTO.response.pdf.UploadedPdfResponse;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

public interface PdfService {
    Mono<UploadedPdfResponse> savePdf(MultipartFile file, String secondaryFileName);
}
