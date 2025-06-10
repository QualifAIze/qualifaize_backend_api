package org.qualifaizebackendapi.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ErrorResponse {
    @Schema(description = "Short error code identifying the type of error", example = "BadRequest")
    private final String error;

    @Schema(description = "Detailed error message providing more context", example = "Invalid parameter.")
    private final String message;
}
