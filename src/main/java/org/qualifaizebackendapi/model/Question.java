package org.qualifaizebackendapi.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.qualifaizebackendapi.model.enums.Difficulty;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "question")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interview_id", nullable = false)
    private Interview interview;

    @NotNull
    @Size(min = 10, message = "Question text must be at least 10 characters")
    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @NotNull
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(nullable = false)
    private Difficulty difficulty = Difficulty.MEDIUM;

    @NotNull
    @Column(name = "option_a", nullable = false, columnDefinition = "TEXT")
    private String optionA;

    @NotNull
    @Column(name = "option_b", nullable = false, columnDefinition = "TEXT")
    private String optionB;

    @NotNull
    @Column(name = "option_c", nullable = false, columnDefinition = "TEXT")
    private String optionC;

    @NotNull
    @Column(name = "option_d", nullable = false, columnDefinition = "TEXT")
    private String optionD;

    @NotNull
    @Pattern(regexp = "[ABCD]", message = "Correct option must be A, B, C, or D")
    @Column(name = "correct_option", nullable = false, length = 1)
    private String correctOption;

    @Column(columnDefinition = "TEXT")
    private String explanation;

    @NotNull
    @Min(value = 1, message = "Question order must be at least 1")
    @Column(name = "question_order", nullable = false)
    private Integer questionOrder = 1;

    @Pattern(regexp = "[ABCD]?", message = "Submitted answer must be A, B, C, D, or null")
    @Column(name = "submitted_answer", length = 1)
    private String submittedAnswer;

    @Column(name = "answered_at")
    private OffsetDateTime answeredAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    /**
     * Submits an answer for this question and records the submission time.
     *
     * @param answer The submitted answer (A, B, C, or D)
     * @throws IllegalArgumentException if the answer is not valid
     */
    public void submitAnswer(String answer) {
        if (answer == null || !answer.matches("[ABCD]")) {
            throw new IllegalArgumentException("Answer must be A, B, C, or D");
        }
        this.submittedAnswer = answer.toUpperCase();
        this.answeredAt = OffsetDateTime.now();
    }

    /**
     * Submits an answer using a character parameter.
     *
     * @param answer The submitted answer as a char (A, B, C, or D)
     */
    public void submitAnswer(char answer) {
        submitAnswer(String.valueOf(answer));
    }

    /**
     * Checks if this question has been answered.
     *
     * @return true if the question has been answered, false otherwise
     */
    public boolean isAnswered() {
        return submittedAnswer != null && answeredAt != null;
    }

    /**
     * Checks if the submitted answer is correct.
     *
     * @return true if answered and correct, false if answered and incorrect
     * @throws IllegalStateException if the question hasn't been answered yet
     */
    public boolean isSubmittedAnswerCorrect() {
        if (!isAnswered()) {
            throw new IllegalStateException("Question has not been answered yet");
        }
        return this.correctOption.equalsIgnoreCase(submittedAnswer);
    }

    /**
     * Gets how long it took to answer the question (if answered).
     *
     * @return Duration in milliseconds between creation and answer, or null if not answered
     */
    public Long getAnswerTimeInMillis() {
        if (!isAnswered()) {
            return null;
        }
        return java.time.Duration.between(createdAt, answeredAt).get(ChronoUnit.MILLIS);
    }
}