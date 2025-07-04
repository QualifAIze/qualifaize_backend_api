package org.qualifaizebackendapi.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.qualifaizebackendapi.DTO.response.pdf.UploadedPdfResponse;
import org.qualifaizebackendapi.DTO.response.pdf.UploadedPdfResponseWithConcatenatedContent;
import org.qualifaizebackendapi.exception.DuplicateException;
import org.qualifaizebackendapi.exception.ResourceNotFoundException;
import org.qualifaizebackendapi.mapper.PdfMapper;
import org.qualifaizebackendapi.model.Document;
import org.qualifaizebackendapi.model.Subsection;
import org.qualifaizebackendapi.repository.PdfRepository;
import org.qualifaizebackendapi.service.PdfService;
import org.qualifaizebackendapi.utils.SecurityUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PdfServiceImpl implements PdfService {

    private final RestClient documentParserRestClient;
    private final PdfRepository pdfRepository;
    private final PdfMapper pdfMapper;

    @Override
    public UploadedPdfResponse savePdf(MultipartFile file, String secondaryFileName) {
        log.info("Starting PDF upload process for file: {} with secondary name: {}",
                file.getOriginalFilename(), secondaryFileName);

        validateUniqueSecondaryFileName(secondaryFileName);

        try {
            validateUploadedFile(file);
            Document documentWithSubsections = callDocumentParserService(file);
            documentWithSubsections.setUploadedByUser(SecurityUtils.getCurrentUser());
            documentWithSubsections.setSecondaryFileName(secondaryFileName);
            documentWithSubsections = pdfRepository.save(documentWithSubsections);

            this.logDocumentSaveSuccess(documentWithSubsections);
            return pdfMapper.toUploadedPdfResponse(this.findDocumentByIdOrThrow(documentWithSubsections.getId()));

        } catch (Exception error) {
            log.error("PDF upload failed for file: {} - {}", file.getOriginalFilename(), error.getMessage(), error);
            throw handleProcessingError(error);
        }
    }

    @Override
    public UploadedPdfResponseWithConcatenatedContent getConcatenatedContentById(UUID documentId, String subsectionTitle) {
        log.info("Retrieving concatenated content for document ID: {} and subsection: {}", documentId, subsectionTitle);

        Document document = this.findDocumentByIdOrThrow(documentId);
        Subsection chosenSubsection = this.findSubsectionByTitleInDocument(document.getSubsections(), subsectionTitle);
        StringBuilder allContent = new StringBuilder(chosenSubsection.getContent());
        collectAllSubsectionContent(chosenSubsection.getChildren(), allContent);
        return new UploadedPdfResponseWithConcatenatedContent(subsectionTitle, allContent.toString());
    }

    @Override
    public UploadedPdfResponse changeDocumentSecondaryFilename(UUID documentId, String newTitle) {
        log.info("Updating secondary filename for document ID: {} to: {}", documentId, newTitle);

        Document document = this.findDocumentByIdOrThrow(documentId);
        document.setSecondaryFileName(newTitle);
        pdfRepository.save(document);

        return pdfMapper.toUploadedPdfResponse(document);
    }

    @Override
    public List<UploadedPdfResponse> getAllDocuments() {
        log.debug("Retrieving all documents");

        List<Document> documents = pdfRepository.findAllActive();
        return pdfMapper.toUploadedPdfResponseList(documents);
    }

    @Override
    @Transactional
    public void deleteDocument(UUID documentId) {
        log.info("Deleting document with ID: {}", documentId);

        Document document = findDocumentByIdOrThrow(documentId);
        pdfRepository.softDeleteById(document.getId());

        log.info("Document deleted successfully: {}", documentId);
    }

    private void validateUniqueSecondaryFileName(String secondaryFileName) {
        if (pdfRepository.existsBySecondaryFileName(secondaryFileName)) {
            String message = "Document with name '" + secondaryFileName + "' already exists";
            log.warn("Duplicate secondary filename detected: {}", secondaryFileName);
            throw new DuplicateException(message);
        }
    }

    private void validateUploadedFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || filename.trim().isEmpty()) {
            throw new IllegalArgumentException("File must have a valid filename");
        }

        if (!Objects.equals(file.getContentType(), "application/pdf")) {
            throw new IllegalArgumentException("File must be a PDF document");
        }

        log.debug("File validation passed for: {}", filename);
    }

    private Document callDocumentParserService(MultipartFile file) {
        log.debug("Sending file to document parser service: {}", file.getOriginalFilename());

        try {
            MultiValueMap<String, Object> requestBody = createMultipartRequestBody(file);

            Document response = documentParserRestClient
                    .post()
                    .uri("/parse")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(requestBody)
                    .retrieve()
                    .body(Document.class);

            assignDocumentAndParent(response, response.getSubsections(), null);
            String responseFilename = response != null ? response.getFileName() : "unknown";
            log.info("Document parsing completed successfully: {}", responseFilename);

            return response;

        } catch (IOException e) {
            throw new RuntimeException("Failed to read file bytes from uploaded file", e);
        } catch (RestClientResponseException e) {
            String errorMessage = String.format("Document parser service error [%d]: %s",
                    e.getStatusCode().value(), e.getResponseBodyAsString());
            throw new RuntimeException(errorMessage, e);
        } catch (RestClientException e) {
            throw new RuntimeException("Document parser service request failed: " + e.getMessage(), e);
        }
    }

    private MultiValueMap<String, Object> createMultipartRequestBody(MultipartFile file) throws IOException {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        ByteArrayResource fileResource = new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        };

        body.add("file", fileResource);
        return body;
    }

    private void collectAllSubsectionContent(List<Subsection> subsections, StringBuilder contentBuilder) {
        if (subsections == null || contentBuilder == null) return;

        for (Subsection subsection : subsections) {
            if (subsection.getContent() != null) contentBuilder.append(subsection.getContent());

            //DFS
            collectAllSubsectionContent(subsection.getChildren(), contentBuilder);
        }
    }

    private Subsection findSubsectionByTitleInDocument(List<Subsection> subsections, String title) {
        if (subsections == null || title == null) return null;

        for (Subsection subsection : subsections) {
            if (title.equalsIgnoreCase(subsection.getTitle())) return subsection;

            Subsection found = findSubsectionByTitleInDocument(subsection.getChildren(), title);
            if (found != null) return found;
        }

        return null;
    }

    private List<Subsection> buildSubsectionsHierarchy(List<Subsection> subsections) {
        if (subsections == null || subsections.isEmpty()) {
            return new ArrayList<>();
        }

        Map<UUID, List<Subsection>> childrenMap = new HashMap<>();
        List<Subsection> roots = new ArrayList<>();

        for (Subsection s : subsections) {
            if (s.getParent() == null) {
                roots.add(s);
            } else childrenMap.computeIfAbsent(s.getParent().getId(), k -> new ArrayList<>()).add(s);
        }

        roots.sort(Comparator.comparingInt(Subsection::getPosition));
        childrenMap.values().forEach(children ->
                children.sort(Comparator.comparingInt(Subsection::getPosition))
        );

        for (Subsection root : roots) {
            buildSubsectionsHierarchyRecursive(root, childrenMap);
        }

        return roots;
    }

    private void buildSubsectionsHierarchyRecursive(Subsection current, Map<UUID, List<Subsection>> childrenMap) {
        List<Subsection> children = childrenMap.get(current.getId());

        if (children != null && !children.isEmpty()) {
            current.setChildren(new ArrayList<>(children));

            for (Subsection child : children) {
                buildSubsectionsHierarchyRecursive(child, childrenMap);
            }
        }
    }

    private void assignDocumentAndParent(Document document, List<Subsection> subsections, Subsection parent) {
        if (subsections == null || subsections.isEmpty()) {
            return;
        }

        for (Subsection subsection : subsections) {
            subsection.setDocument(document);
            subsection.setParent(parent);

            if (subsection.getChildren() != null && !subsection.getChildren().isEmpty()) {
                assignDocumentAndParent(document, subsection.getChildren(), subsection);
            }
        }
    }

    public Document findDocumentByIdOrThrow(UUID documentId) {
        Document document = pdfRepository.findActiveById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("PDF document not found with ID: %s", documentId)
                ));
        document.setSubsections(buildSubsectionsHierarchy(document.getSubsections()));
        return document;
    }

    private RuntimeException handleProcessingError(Throwable error) {
        String baseErrorMessage = "Failed to process PDF document";

        return switch (error) {
            case DuplicateException e -> e;
            case IllegalArgumentException e -> new IllegalArgumentException(e.getMessage(), e);
            case RestClientResponseException e -> new RuntimeException(
                    String.format("%s - Service error [%d]: %s",
                            baseErrorMessage, e.getStatusCode().value(), e.getResponseBodyAsString()), e);
            case RestClientException e -> new RuntimeException(
                    baseErrorMessage + " - Request failed: " + e.getMessage(), e);
            case null, default -> new RuntimeException(baseErrorMessage, error);
        };
    }

    private void logDocumentSaveSuccess(Document document) {
        int subsectionCount = document.getSubsections() != null ? document.getSubsections().size() : 0;

        log.info("Document saved successfully:");
        log.info("  - Document ID: {}", document.getId());
        log.info("  - Original filename: {}", document.getFileName());
        log.info("  - Secondary filename: {}", document.getSecondaryFileName());
        log.info("  - Total subsections: {}", subsectionCount);
    }
}