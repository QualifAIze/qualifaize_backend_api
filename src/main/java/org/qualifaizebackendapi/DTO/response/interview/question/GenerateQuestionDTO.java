package org.qualifaizebackendapi.DTO.response.interview.question;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GenerateQuestionDTO {
    private String question;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private char correctAnswer; // 'A', 'B', 'C', or 'D'
}