package org.qualifaizebackendapi.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.qualifaizebackendapi.DTO.db_object.DocumentWithUserRow;
import org.qualifaizebackendapi.DTO.db_object.SubsectionRow;
import org.qualifaizebackendapi.DTO.parseDTO.ParsedDocumentDetailsResponse;
import org.qualifaizebackendapi.DTO.parseDTO.SubsectionWithContent;
import org.qualifaizebackendapi.DTO.response.pdf.UploadedPdfResponse;
import org.qualifaizebackendapi.DTO.response.pdf.UploadedPdfResponseWithConcatenatedContent;
import org.qualifaizebackendapi.DTO.response.pdf.table_of_contents_response.SubsectionDetailsDTO;
import org.qualifaizebackendapi.DTO.response.pdf.table_of_contents_response.UploadedPdfResponseWithToc;
import org.qualifaizebackendapi.exception.DuplicateDocumentException;
import org.qualifaizebackendapi.exception.ResourceNotFoundException;
import org.qualifaizebackendapi.mapper.PdfMapper;
import org.qualifaizebackendapi.mapper.SubsectionMapper;
import org.qualifaizebackendapi.model.Document;
import org.qualifaizebackendapi.model.Subsection;
import org.qualifaizebackendapi.repository.PdfRepository;
import org.qualifaizebackendapi.repository.SubsectionRepository;
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
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PdfServiceImpl implements PdfService {

    private final RestClient documentParserRestClient;
    private final PdfRepository pdfRepository;
    private final SubsectionRepository subsectionRepository;
    private final PdfMapper pdfMapper;
    private final SubsectionMapper subsectionMapper;

    // ==================== Public Service Methods ====================

    @Override
    public UploadedPdfResponse savePdf(MultipartFile file, String secondaryFileName) {
        log.info("Starting PDF upload process for file: {} with secondary name: {}",
                file.getOriginalFilename(), secondaryFileName);

        validateUniqueSecondaryFileName(secondaryFileName);

        try {
            validateUploadedFile(file);
            ParsedDocumentDetailsResponse parsedResponse = callDocumentParserService(file);
            Document savedDocument = saveDocumentWithSubsections(parsedResponse, secondaryFileName);

            log.info("PDF upload completed successfully for document ID: {}", savedDocument.getId());
            return this.pdfMapper.toUploadedPdfResponse(findDocumentWithUserRowOrThrow(savedDocument.getId()));

        } catch (Exception error) {
            log.error("PDF upload failed for file: {} - {}", file.getOriginalFilename(), error.getMessage(), error);
            throw handleProcessingError(error);
        }
    }

    @Override
    public UploadedPdfResponseWithToc getDocumentDetailsAndTocById(UUID documentId) {
        log.debug("Retrieving document details with table of contents for ID: {}", documentId);

        UploadedPdfResponseWithToc response = pdfMapper.toUploadedPdfResponseWithToc(this.findDocumentWithUserRowOrThrow(documentId));
        List<SubsectionDetailsDTO> hierarchicalSubsections = buildSubsectionHierarchy(subsectionRepository.fetchFlatToc(documentId));
        response.setSubsections(hierarchicalSubsections);
        return response;
    }

    @Override
    public UploadedPdfResponseWithConcatenatedContent getConcatenatedContentById(UUID documentId, String subsectionName) {
        log.debug("Retrieving concatenated content for document ID: {} and subsection: {}", documentId, subsectionName);

        List<Object[]> contentData = pdfRepository.findSubsectionContentByTitleAndDocumentId(subsectionName, documentId);
        return mapToContentResponse(contentData);
    }

    @Override
    public UploadedPdfResponse changeDocumentSecondaryFilename(UUID documentId, String newTitle) {
        log.info("Updating secondary filename for document ID: {} to: {}", documentId, newTitle);

        Document document = this.findDocumentByIdOrThrow(documentId);
        document.setSecondaryFileName(newTitle);
        pdfRepository.save(document);

        return pdfMapper.toUploadedPdfResponse(this.findDocumentWithUserRowOrThrow(documentId));
    }

    @Override
    public List<UploadedPdfResponse> getAllDocuments() {
        log.debug("Retrieving all documents");

        List<DocumentWithUserRow> documents = pdfRepository.getAllDocumentWithUserDetails();
        return pdfMapper.toUploadedPdfResponses(documents);
    }

    @Override
    public void deleteDocument(UUID documentId) {
        log.info("Deleting document with ID: {}", documentId);

        Document document = findDocumentByIdOrThrow(documentId);
        pdfRepository.delete(document);

        log.info("Document deleted successfully: {}", documentId);
    }

    // ==================== File Upload & Processing ====================

    private void validateUniqueSecondaryFileName(String secondaryFileName) {
        if (pdfRepository.existsBySecondaryFileName(secondaryFileName)) {
            String message = "Document with name '" + secondaryFileName + "' already exists";
            log.warn("Duplicate secondary filename detected: {}", secondaryFileName);
            throw new DuplicateDocumentException(message);
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

    private ParsedDocumentDetailsResponse callDocumentParserService(MultipartFile file) {
        log.debug("Sending file to document parser service: {}", file.getOriginalFilename());

        try {
            MultiValueMap<String, Object> requestBody = createMultipartRequestBody(file);

            ParsedDocumentDetailsResponse response = documentParserRestClient
                    .post()
                    .uri("/parse")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(requestBody)
                    .retrieve()
                    .body(ParsedDocumentDetailsResponse.class);

            String responseFilename = response != null ? response.getOriginalFilename() : "unknown";
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

    // ==================== Document & Subsection Processing ====================

    private Document saveDocumentWithSubsections(ParsedDocumentDetailsResponse response, String secondaryFileName) {
        if (response == null) {
            log.warn("Received null response from document parser service");
            return new Document();
        }

        Document document = createDocumentFromResponse(response, secondaryFileName);
        document.setUploadedByUser(SecurityUtils.getCurrentUser());
        Document savedDocument = pdfRepository.save(document);

        logDocumentSaveSuccess(savedDocument);
        return savedDocument;
    }

    private Document createDocumentFromResponse(ParsedDocumentDetailsResponse response, String secondaryFileName) {
        Document document = pdfMapper.toDocument(response, secondaryFileName);

        List<SubsectionWithContent> parsedSubsections = extractSubsectionsFromResponse(response);
        List<Subsection> mappedSubsections = new ArrayList<>();
        document.setSubsections(mappedSubsections);

        mapSubsectionsRecursively(mappedSubsections, parsedSubsections, document, null, 0);

        return document;
    }

    private List<SubsectionWithContent> extractSubsectionsFromResponse(ParsedDocumentDetailsResponse response) {
        return response.getTocWithContent() != null ?
                response.getTocWithContent().getSubsections() :
                new ArrayList<>();
    }

    private void mapSubsectionsRecursively(List<Subsection> mappedSubsections,
                                           List<SubsectionWithContent> subsectionsToMap,
                                           Document document,
                                           Subsection parent,
                                           int level) {
        if (subsectionsToMap == null || subsectionsToMap.isEmpty()) {
            return;
        }

        for (int position = 0; position < subsectionsToMap.size(); position++) {
            SubsectionWithContent subsectionToMap = subsectionsToMap.get(position);
            Subsection mappedSubsection = subsectionMapper.mapToSubsection(subsectionToMap, parent, document, level, position);

            addSubsectionToParent(mappedSubsection, parent, mappedSubsections);
            mapSubsectionsRecursively(mappedSubsections, subsectionToMap.getSubsections(),
                    document, mappedSubsection, level + 1);
        }
    }

    private void addSubsectionToParent(Subsection subsection, Subsection parent, List<Subsection> rootSubsections) {
        if (parent == null) {
            rootSubsections.add(subsection);
        } else {
            parent.getChildren().add(subsection);
        }
    }

    private void logDocumentSaveSuccess(Document document) {
        int subsectionCount = document.getSubsections() != null ? document.getSubsections().size() : 0;

        log.info("Document saved successfully:");
        log.info("  - Document ID: {}", document.getId());
        log.info("  - Original filename: {}", document.getFileName());
        log.info("  - Secondary filename: {}", document.getSecondaryFileName());
        log.info("  - Total subsections: {}", subsectionCount);
    }

    // ==================== Table of Contents Processing ====================

    public List<SubsectionDetailsDTO> buildSubsectionHierarchy(List<SubsectionRow> flatSubsections) {
        if (flatSubsections == null || flatSubsections.isEmpty()) {
            return new ArrayList<>();
        }

        Map<UUID, List<SubsectionRow>> childrenByParentId = groupSubsectionsByParent(flatSubsections);
        sortSubsectionsByPosition(childrenByParentId);
        List<SubsectionRow> rootSubsections = findRootSubsections(flatSubsections);

        return convertToHierarchicalStructure(rootSubsections, childrenByParentId);
    }

    private Map<UUID, List<SubsectionRow>> groupSubsectionsByParent(List<SubsectionRow> subsections) {
        UUID nullParentKey = UUID.fromString("00000000-0000-0000-0000-000000000000");

        return subsections.stream()
                .collect(Collectors.groupingBy(
                        row -> row.getParentId() != null ? row.getParentId() : nullParentKey,
                        Collectors.toList()
                ));
    }

    private void sortSubsectionsByPosition(Map<UUID, List<SubsectionRow>> childrenByParent) {
        childrenByParent.values().forEach(children ->
                children.sort(Comparator.comparingInt(SubsectionRow::getPosition))
        );
    }

    private List<SubsectionRow> findRootSubsections(List<SubsectionRow> subsections) {
        return subsections.stream()
                .filter(row -> row.getParentId() == null)
                .sorted(Comparator.comparingInt(SubsectionRow::getPosition))
                .toList();
    }

    private List<SubsectionDetailsDTO> convertToHierarchicalStructure(List<SubsectionRow> rootSubsections,
                                                                      Map<UUID, List<SubsectionRow>> childrenByParent) {
        return rootSubsections.stream()
                .map(root -> buildSubsectionWithChildren(root, childrenByParent))
                .collect(Collectors.toList());
    }

    private SubsectionDetailsDTO buildSubsectionWithChildren(SubsectionRow subsection,
                                                             Map<UUID, List<SubsectionRow>> childrenByParent) {
        SubsectionDetailsDTO dto = subsectionMapper.toSubsectionDetailsDTO(subsection);

        List<SubsectionRow> children = childrenByParent.getOrDefault(subsection.getId(), new ArrayList<>());
        List<SubsectionDetailsDTO> childDTOs = children.stream()
                .map(child -> buildSubsectionWithChildren(child, childrenByParent))
                .collect(Collectors.toList());

        dto.setSubsections(childDTOs);
        return dto;
    }

    // ==================== Utility Methods ====================

    public Document findDocumentByIdOrThrow(UUID documentId) {
        return pdfRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("PDF document not found with ID: %s", documentId)
                ));
    }

    private DocumentWithUserRow findDocumentWithUserRowOrThrow(UUID documentId) {
        return pdfRepository.getDocumentWithUserDetails(documentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("PDF document not found with ID: %s", documentId)
                ));
    }

    private UploadedPdfResponseWithConcatenatedContent mapToContentResponse(List<Object[]> contentData) {
        if (contentData == null || contentData.isEmpty()) {
            throw new ResourceNotFoundException("No content found for the specified document and subsection");
        }

        Object[] row = contentData.getFirst();
        return new UploadedPdfResponseWithConcatenatedContent(
                (UUID) row[3],           // documentId
                (String) row[0],         // filename
                (String) row[1],         // secondaryFilename
                ((Instant) row[2]).atOffset(ZoneOffset.UTC), // createdAt
                (String) row[4],         // sectionTitle
                (String) row[5],         // content
                (Long) row[6]            // subsectionsCount
        );
    }

    private RuntimeException handleProcessingError(Throwable error) {
        String baseErrorMessage = "Failed to process PDF document";

        return switch (error) {
            case DuplicateDocumentException e -> e;
            case IllegalArgumentException e -> new IllegalArgumentException(e.getMessage(), e);
            case RestClientResponseException e -> new RuntimeException(
                    String.format("%s - Service error [%d]: %s",
                            baseErrorMessage, e.getStatusCode().value(), e.getResponseBodyAsString()), e);
            case RestClientException e -> new RuntimeException(
                    baseErrorMessage + " - Request failed: " + e.getMessage(), e);
            case null, default -> new RuntimeException(baseErrorMessage, error);
        };
    }
}