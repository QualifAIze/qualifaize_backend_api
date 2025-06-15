package org.qualifaizebackendapi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.qualifaizebackendapi.DTO.response.pdf.UploadedPdfResponse;
import org.qualifaizebackendapi.DTO.response.pdf.UploadedPdfResponseWithConcatenatedContent;
import org.qualifaizebackendapi.DTO.response.pdf.table_of_contents_response.UploadedPdfResponseWithToc;
import org.qualifaizebackendapi.exception.ErrorResponse;
import org.qualifaizebackendapi.service.PdfService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Tag(name = "PDF Operations", description = "Operations related to pdf document/s")
@RequiredArgsConstructor
@RequestMapping("/api/v1/pdf")
@RestController
public class PdfController {

    private final PdfService pdfService;

    @Operation(
            summary = "Upload a PDF file",
            description = "Uploads one PDF file. The 'file' parameter must be a valid PDF document. The document is parsed and saved"
    )
    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "PDF uploaded successfully",
                    content = @Content(schema = @Schema(implementation = UploadedPdfResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid file or parameters",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Already saved pdf document",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Mono<UploadedPdfResponse> uploadPdf(
            @Parameter(description = "PDF file to upload", required = true, content = @Content(mediaType = "application/pdf"))
            @RequestParam("file") MultipartFile file,

            @Parameter(description = "Required secondary file name", required = true)
            @RequestParam(value = "secondary_file_name") String secondaryFileName
    ) {
        return pdfService.savePdf(file, secondaryFileName);
    }


    @Operation(
            summary = "List all PDF documents",
            description = "Retrieves a list of all uploaded PDF documents."
    )
    @GetMapping
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Documents retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = UploadedPdfResponse.class)))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<List<UploadedPdfResponse>> getAllDocuments() {
        List<UploadedPdfResponse> docs = pdfService.getAllDocuments();
        return ResponseEntity.ok(docs);
    }


    @Operation(
            summary = "Get PDF document concatenated content",
            description = "Retrieves the content of the specified subsection and all its nested sections into a single string."
    )
    @GetMapping("/{documentId}/content")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Document content retrieved and concatenated successfully",
                    content = @Content(schema = @Schema(implementation = UploadedPdfResponseWithConcatenatedContent.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid document ID or subsection name parameter",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Document or subsection not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<UploadedPdfResponseWithConcatenatedContent> getDocumentContent(
            @Parameter(
                    description = "Unique identifier of the document whose content to retrieve and concatenate",
                    required = true
            )
            @PathVariable @NotNull UUID documentId,

            @Parameter(
                    description = "Name of the subsection from which to start concatenating content",
                    required = true
            )
            @RequestParam(name = "subsectionName")
            @NotBlank String subsectionName
    ) {
        UploadedPdfResponseWithConcatenatedContent dto =
                pdfService.getConcatenatedContentById(documentId, subsectionName);
        return ResponseEntity.ok(dto);
    }


    @Operation(
            summary = "Get PDF document details with table of contents",
            description = "Retrieves metadata of a specific PDF document by its unique identifier, including its table of contents."
    )
    @GetMapping("/{documentId}")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Document retrieved successfully",
                    content = @Content(schema = @Schema(implementation = UploadedPdfResponseWithToc.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid document ID",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Document not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<UploadedPdfResponseWithToc> getDocumentByIdWithToc(
            @Parameter(
                    description = "Unique identifier of the document to retrieve",
                    required = true
            )
            @PathVariable @NotNull UUID documentId
    ) {
        UploadedPdfResponseWithToc dto = pdfService.getDocumentDetailsAndTocById(documentId);
        return ResponseEntity.ok(dto);
    }


    @Operation(
            summary = "Update document title",
            description = "Updates the secondary filename (title) of an existing PDF document. Only the title field is modified, leaving other document properties unchanged."
    )
    @PatchMapping("/{documentId}")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Document title updated successfully",
                    content = @Content(schema = @Schema(implementation = UploadedPdfResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid document ID or title parameter",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Document not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<UploadedPdfResponse> modifySecondaryFilename(
            @Parameter(description = "Unique identifier of the document to update", required = true)
            @PathVariable @NotNull UUID documentId,

            @Parameter(description = "New title for the document", required = true)
            @RequestParam @NotBlank String title) {

        return ResponseEntity.ok(pdfService.changeDocumentSecondaryFilename(documentId, title));
    }


    @Operation(
            summary = "Delete PDF document",
            description = "Deletes a specific PDF document by its unique identifier."
    )
    @DeleteMapping("/{documentId}")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Document deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid document ID",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Document not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<Void> deleteDocument(
            @Parameter(
                    description = "Unique identifier of the document to delete",
                    required = true
            )
            @PathVariable @NotNull UUID documentId
    ) {
        pdfService.deleteDocument(documentId);
        return ResponseEntity.noContent().build();
    }
}
