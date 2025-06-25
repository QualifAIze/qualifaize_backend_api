package org.qualifaizebackendapi.DTO.response.interview.question;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.qualifaizebackendapi.model.enums.Difficulty;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Detailed question information including answers and results")
public class QuestionDetailsResponse {

    @Schema(description = "Question text", example = "What is the primary purpose of the @Autowired annotation?")
    private String questionText;

    @Schema(description = "Question difficulty level", example = "MEDIUM")
    private Difficulty difficulty;

    @Schema(description = "Option A text", example = "Dependency Injection")
    private String optionA;

    @Schema(description = "Option B text", example = "Component Scanning")
    private String optionB;

    @Schema(description = "Option C text", example = "Bean Configuration")
    private String optionC;

    @Schema(description = "Option D text", example = "Transaction Management")
    private String optionD;

    @Schema(description = "Correct answer option", example = "A")
    private String correctOption;

    @Schema(description = "Question order in the interview", example = "1")
    private Integer questionOrder;

    @Schema(description = "User's submitted answer (if answered)", example = "A")
    private String submittedAnswer;

    @Schema(description = "Whether the submitted answer is correct")
    private Boolean isCorrect;

    @Schema(description = "Time taken to answer in milliseconds")
    private Long answerTimeInMillis;
}