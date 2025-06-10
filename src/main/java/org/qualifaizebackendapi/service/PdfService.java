package org.qualifaizebackendapi.service;

import org.qualifaizebackendapi.DTO.response.pdf.UploadedPdfResponse;
import org.springframework.web.multipart.MultipartFile;

public interface PdfService {
    UploadedPdfResponse savePdf(MultipartFile file, String secondaryFileName);
}
