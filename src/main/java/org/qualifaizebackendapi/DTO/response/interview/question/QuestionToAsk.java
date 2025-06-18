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
@Schema(description = "Response containing the next question to ask in an interview")
public class QuestionToAsk {

    @Schema(description = "Unique identifier of the question", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID questionId;

    @Schema(description = "The question title/text to be asked", example = "What is the primary purpose of the Spring Framework?")
    private String title;

    @Schema(description = "Option A for the multiple choice question", example = "Dependency Injection")
    private String optionA;

    @Schema(description = "Option B for the multiple choice question", example = "Web Development")
    private String optionB;

    @Schema(description = "Option C for the multiple choice question", example = "Database Management")
    private String optionC;

    @Schema(description = "Option D for the multiple choice question", example = "User Interface Design")
    private String optionD;
}