package org.qualifaizebackendapi.DTO.response.interview.question;

import lombok.Getter;
import lombok.Setter;
import org.qualifaizebackendapi.model.enums.Difficulty;

@Getter
@Setter
public class GenerateQuestionDTO {
    private String questionText;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String correctOption; // 'A', 'B', 'C', or 'D'
    private Difficulty difficulty;
    private String explanation;
}