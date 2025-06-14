package org.qualifaizebackendapi.exception;

import io.swagger.v3.oas.annotations.media.Schema;

public record ErrorResponse(
        @Schema(description = "Short error code identifying the type of error", example = "BadRequest") String error,
        @Schema(description = "Detailed error message providing more context", example = "Invalid parameter.") String message) {
}
