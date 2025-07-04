package org.qualifaizebackendapi.DTO;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.qualifaizebackendapi.model.enums.Difficulty;

import java.time.Duration;
import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
public class QuestionDetailsDTO {
    private String questionText;
    private String correctOption;
    private String submittedAnswer;
    private Long answerTimeInMillis;
    private Difficulty difficulty;

    public QuestionDetailsDTO(String questionText, String correctOption, String submittedAnswer, OffsetDateTime answerCreatedAt, OffsetDateTime answerAnsweredAt, Difficulty difficulty) {
        this.questionText = questionText;
        this.correctOption = correctOption;
        this.submittedAnswer = submittedAnswer;
        this.difficulty = difficulty;
        this.answerTimeInMillis = Duration.between(answerCreatedAt, answerAnsweredAt).toMillis();
    }

    public boolean isSubmittedAnswerCorrect() {
        return submittedAnswer.equalsIgnoreCase(correctOption);
    }
}
