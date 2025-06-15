package org.qualifaizebackendapi.service.impl;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.qualifaizebackendapi.DTO.parseDTO.ParsedDocumentDetailsResponse;
import org.qualifaizebackendapi.DTO.parseDTO.SubsectionWithContent;
import org.qualifaizebackendapi.DTO.response.pdf.UploadedPdfResponse;
import org.qualifaizebackendapi.exception.DuplicateDocumentException;
import org.qualifaizebackendapi.mapper.PdfMapper;
import org.qualifaizebackendapi.model.Document;
import org.qualifaizebackendapi.model.Subsection;
import org.qualifaizebackendapi.repository.PdfRepository;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PdfServiceImpl implements PdfService {

    private final WebClient documentParserServiceWebClient;
    private final PdfRepository pdfRepository;
    private final PdfMapper pdfMapper;

    @Override
    public Mono<UploadedPdfResponse> savePdf(MultipartFile file, String secondaryFileName) {

        if (pdfRepository.existsBySecondaryFileName(secondaryFileName)) {
            log.warn("Document with secondary filename '{}' already exists", secondaryFileName);
            throw new DuplicateDocumentException("Document with name '" + secondaryFileName + "' already exists");
        }

        log.info("Processing PDF upload for file: {} with secondary name: {}",
                file.getOriginalFilename(), secondaryFileName);

        return createFileResource(file)
                .flatMap(this::sendToDocumentParser)
                .flatMap(response -> Mono.just(processDocumentResponse(response, secondaryFileName)))
                .map(response -> pdfMapper.toUploadedPdfResponse(response, secondaryFileName))
                .doOnError(error -> log.error("Failed to process PDF: {}", error.getMessage(), error))
                .onErrorMap(this::handleProcessingError);
    }

    @Override
    public UploadedPdfResponse changeDocumentSecondaryFilename(UUID documentId, String newTitle) {
        return null;
    }

    @Override
    public UploadedPdfResponse getDocumentById(UUID documentId) {
        return null;
    }

    @Override
    public List<UploadedPdfResponse> getAllDocuments() {
        return List.of();
    }

    @Override
    public void deleteDocument(UUID documentId) {

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
                        response != null ? response.getOriginalFilename() : "unknown"));
    }

    private Mono<? extends Throwable> handleHttpError(ClientResponse response) {
        return response.bodyToMono(String.class)
                .defaultIfEmpty("Unknown error")
                .map(errorBody -> new RuntimeException(
                        String.format("Document parser service error [%d]: %s",
                                response.statusCode().value(), errorBody)));
    }

    private Document processDocumentResponse(ParsedDocumentDetailsResponse response, String secondaryFileName) {
        if (response == null) {
            log.warn("Received null response from document parser");
            return new Document();
        }

        Document savedDocument = pdfRepository.save(this.mapToDocument(response, secondaryFileName));

        log.info("Document processing completed successfully:");
        log.info("  DocumentId: {}", savedDocument.getId());
        log.info("  Original filename: {}", savedDocument.getFileName());
        log.info("  Secondary filename: {}", savedDocument.getSecondaryFileName());
        log.info("  Content sections: {}", savedDocument.getSubsections() != null ? savedDocument.getSubsections().size() : 0);

        return savedDocument;
    }

    private Document mapToDocument(ParsedDocumentDetailsResponse response, String secondaryFileName) {
        Document document = pdfMapper.toDocument(response, secondaryFileName);
        List<SubsectionWithContent> allSubsectionsBeforeMapping = response.getTocWithContent().getSubsections();
        List<Subsection> mappedSubsections = new ArrayList<>();
        document.setSubsections(mappedSubsections);
        mapSubsections(mappedSubsections, allSubsectionsBeforeMapping, document, null, 0);
        return document;
    }

    private void mapSubsections(List<Subsection> mappedSubsections, List<SubsectionWithContent> subsectionsToBeMapped,
                                Document document, Subsection parent, int level) {
        if (subsectionsToBeMapped == null) return;

        for (int i = 0; i < subsectionsToBeMapped.size(); i++) {
            SubsectionWithContent unmappedSubsection = subsectionsToBeMapped.get(i);
            Subsection mappedSubsection = pdfMapper.mapToSubsection(unmappedSubsection, parent, document, level, i);
            if (parent == null) {
                mappedSubsections.add(mappedSubsection);
            } else {
                parent.getChildren().add(mappedSubsection);
            }
            mapSubsections(mappedSubsections, unmappedSubsection.getSubsections(), document, mappedSubsection, level + 1);

        }
    }

    private RuntimeException handleProcessingError(Throwable error) {
        String errorMessage = "Failed to process PDF document";

        return switch (error) {
            case DuplicateDocumentException ignore ->  new DuplicateDocumentException(error.getMessage());
            case IllegalArgumentException ignored -> new IllegalArgumentException(error.getMessage(), error);
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