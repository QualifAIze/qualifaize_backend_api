package org.qualifaizebackendapi.DTO.request.interview;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.qualifaizebackendapi.model.enums.Difficulty;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for creating a new interview")
public class CreateInterviewRequest {

    @NotBlank(message = "Interview name is required")
    @Size(min = 1, max = 255, message = "Interview name must be between 1 and 255 characters")
    @Schema(description = "Name of the interview", example = "Java Spring Boot Interview", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "Optional description of the interview", example = "Technical interview focusing on Spring Boot concepts")
    private String description;

    @NotNull(message = "Document ID is required")
    @Schema(description = "UUID of the document this interview is based on", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID documentId;

    @Schema(description = "Difficulty level of the interview", example = "MEDIUM", defaultValue = "MEDIUM")
    private Difficulty difficulty = Difficulty.MEDIUM;

    @Schema(description = "Optional user ID to assign the interview to")
    private UUID assignedToUserId;

    @Future(message = "Scheduled date must be in the future")
    @Schema(description = "Optional scheduled date for the interview")
    private OffsetDateTime scheduledDate;

}
