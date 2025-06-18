package org.qualifaizebackendapi.DTO.response.interview.question;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Response containing the result of the submitted answer")
public class SubmitAnswerResponse {

    @Schema(description = "Unique identifier of the question that was answered", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID questionId;

    @Schema(description = "The answer submitted by the user", example = "A")
    private char submittedAnswer;

    @Schema(description = "The correct answer for the question", example = "A")
    private char correctAnswer;

    @Schema(description = "Whether the submitted answer was correct", example = "true")
    private boolean isCorrect;

    @Schema(description = "Optional explanation for the answer")
    private String explanation;

    @Schema(description = "Current percentage in the interview", example = "75.0")
    private Double currentProgress;
}