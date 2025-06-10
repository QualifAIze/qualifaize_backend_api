package org.qualifaizebackendapi.service.impl;

import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.qualifaizebackendapi.DTO.response.pdf.UploadedPdfResponse;
import org.qualifaizebackendapi.service.PdfService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class PdfServiceImpl implements PdfService {
    @Override
    public UploadedPdfResponse savePdf(MultipartFile file, String secondaryFileName) {

        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            int pageCount = document.getNumberOfPages();
            System.out.printf("Page count is %d\n", pageCount);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return new UploadedPdfResponse(1L, "asd", "secAsd", OffsetDateTime.MAX);
    }
}
