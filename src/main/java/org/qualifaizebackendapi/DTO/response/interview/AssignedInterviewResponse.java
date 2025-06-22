package org.qualifaizebackendapi.DTO.response.interview;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.qualifaizebackendapi.model.enums.Difficulty;
import org.qualifaizebackendapi.model.enums.InterviewStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response object containing interview details assigned to a user")
public class AssignedInterviewResponse {

    @Schema(description = "Unique identifier of the interview", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID interviewId;

    @Schema(description = "Name of the interview", example = "Java Spring Boot Technical Interview")
    private String name;

    @Schema(description = "Description of the interview", example = "Technical interview focusing on Spring Boot concepts")
    private String description;

    @Schema(description = "Difficulty level of the interview", example = "MEDIUM")
    private Difficulty difficulty;

    @Schema(description = "Current status of the interview", example = "SCHEDULED")
    private InterviewStatus status;

    @Schema(description = "Scheduled date for the interview")
    private OffsetDateTime scheduledDate;

    @Schema(description = "Username of the person who created the interview", example = "john_admin")
    private String createdBy;
}