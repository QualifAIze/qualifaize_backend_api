package org.qualifaizebackendapi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.qualifaizebackendapi.DTO.response.pdf.UploadedPdfResponse;
import org.qualifaizebackendapi.exception.ErrorResponse;
import org.qualifaizebackendapi.service.PdfService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

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
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
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

}
