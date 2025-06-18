package org.qualifaizebackendapi.exception;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Standard error response object")
public class ErrorResponse {

    @Schema(description = "Short error code identifying the type of error", example = "BadRequest")
    private String error;

    @Schema(description = "Detailed error message providing more context", example = "Invalid parameter.")
    private String message;
}