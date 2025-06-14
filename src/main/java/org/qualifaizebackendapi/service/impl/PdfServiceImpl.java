package org.qualifaizebackendapi.service.impl;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.qualifaizebackendapi.DTO.parseDTO.ParsedDocumentDetailsResponse;
import org.qualifaizebackendapi.DTO.response.pdf.UploadedPdfResponse;
import org.qualifaizebackendapi.mapper.PdfMapper;
import org.qualifaizebackendapi.service.PdfService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class PdfServiceImpl implements PdfService {

    private final WebClient documentParserServiceWebClient;
    private final PdfMapper pdfMapper;

    @Override
    public Mono<UploadedPdfResponse> savePdf(MultipartFile file, String secondaryFileName) {
        log.info("Processing PDF upload for file: {} with secondary name: {}",
                file.getOriginalFilename(), secondaryFileName);

        return createFileResource(file)
                .flatMap(this::sendToDocumentParser)
                .doOnNext(response -> processDocumentResponse(response, secondaryFileName))
                .map(response -> pdfMapper.toUploadedPdfResponse(response, secondaryFileName))
                .doOnError(error -> log.error("Failed to process PDF: {}", error.getMessage(), error))
                .onErrorMap(this::handleProcessingError);
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }
        if (file.getOriginalFilename() == null || file.getOriginalFilename().trim().isEmpty()) {
            throw new IllegalArgumentException("File must have a valid filename");
        }
        if (!Objects.equals(file.getContentType(), "application/pdf")) {
            throw new IllegalArgumentException("File must be a PDF document");
        }
    }

    private Mono<ParsedDocumentDetailsResponse> sendToDocumentParser(ByteArrayResource fileResource) {
        log.debug("Sending file to document parser service: {}", fileResource.getFilename());

        return documentParserServiceWebClient
                .post()
                .uri("/parse")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData("file", fileResource))
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleHttpError)
                .bodyToMono(ParsedDocumentDetailsResponse.class)
                .doOnSuccess(response -> log.info("Successfully parsed document: {}",
                        response != null ? response.getOriginalFileName() : "unknown"));
    }

    private Mono<? extends Throwable> handleHttpError(ClientResponse response) {
        return response.bodyToMono(String.class)
                .defaultIfEmpty("Unknown error")
                .map(errorBody -> new RuntimeException(
                        String.format("Document parser service error [%d]: %s",
                                response.statusCode().value(), errorBody)));
    }

    private void processDocumentResponse(ParsedDocumentDetailsResponse response, String secondaryFileName) {
        if (response == null) {
            log.warn("Received null response from document parser");
            return;
        }

        log.info("Document processing completed successfully:");
        log.info("  Original filename: {}", response.getOriginalFileName());
        log.info("  Secondary filename: {}", secondaryFileName);
        log.info("  TOC sections: {}", response.getToc() != null ? response.getToc().getSubsectionsCount() : 0);
        log.info("  Content sections: {}", response.getTocWithContent() != null ?
                response.getTocWithContent().getSubsectionsCount() : 0);
    }

    private RuntimeException handleProcessingError(Throwable error) {
        String errorMessage = "Failed to process PDF document";

        return switch (error) {
            case IllegalArgumentException ignored ->
                    new IllegalArgumentException(error.getMessage(), error);
            case WebClientResponseException webError -> new RuntimeException(
                    String.format("%s - Service error [%d]: %s",
                            errorMessage, webError.getStatusCode().value(), webError.getMessage()),
                    error);
            case WebClientRequestException ignored -> new RuntimeException(errorMessage + " - Request timeout", error);
            case null, default -> new RuntimeException(errorMessage, error);
        };
    }

    private Mono<ByteArrayResource> createFileResource(MultipartFile file) {
        return Mono.fromCallable(() -> {
            validateFile(file);
            return new ByteArrayResource(extractFileBytes(file)) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };
        });
    }

    private byte[] extractFileBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw new RuntimeException("Failed to extract bytes from uploaded file", e);
        }
    }
}