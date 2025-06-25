package org.qualifaizebackendapi.DTO.response.interview;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.qualifaizebackendapi.DTO.response.interview.question.QuestionDetailsResponse;
import org.qualifaizebackendapi.DTO.response.user.UserDetailsOverviewResponse;
import org.qualifaizebackendapi.model.enums.Difficulty;
import org.qualifaizebackendapi.model.enums.InterviewStatus;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Complete interview details including all questions")
public class InterviewDetailsResponse {

    @Schema(description = "Interview name", example = "Java Spring Boot Technical Interview")
    private String name;

    @Schema(description = "Interview description", example = "Technical interview focusing on Spring Boot concepts")
    private String description;

    @Schema(description = "Interview difficulty level", example = "MEDIUM")
    private Difficulty difficulty;

    @Schema(description = "Current interview status", example = "IN_PROGRESS")
    private InterviewStatus status;

    @Schema(description = "Document title used for the interview")
    private String documentTitle;

    @Schema(description = "Candidate's performance analysis")
    private String candidateReview;

    @Schema(description = "User who created the interview")
    private UserDetailsOverviewResponse createdBy;

    @Schema(description = "User assigned to take the interview")
    private UserDetailsOverviewResponse assignedTo;

    @Schema(description = "List of all questions in the interview")
    private List<QuestionDetailsResponse> questions;

    @Schema(description = "Total number of questions")
    private Integer totalQuestions;

    @Schema(description = "Interview duration in seconds (only if completed)")
    private Long durationInSeconds;
}