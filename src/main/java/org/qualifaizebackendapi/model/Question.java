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

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public String getCorrectAnswerText() {
        return switch (correctOption.toUpperCase()) {
            case "A" -> optionA;
            case "B" -> optionB;
            case "C" -> optionC;
            case "D" -> optionD;
            default -> throw new IllegalStateException("Invalid correct option: " + correctOption);
        };
    }

    public boolean isCorrectAnswer(String answer) {
        return correctOption.equalsIgnoreCase(answer);
    }

    public String[] getAllOptions() {
        return new String[]{optionA, optionB, optionC, optionD};
    }

    public String getOptionText(String option) {
        return switch (option.toUpperCase()) {
            case "A" -> optionA;
            case "B" -> optionB;
            case "C" -> optionC;
            case "D" -> optionD;
            default -> throw new IllegalArgumentException("Invalid option: " + option);
        };
    }

    public boolean isValidCorrectOption() {
        return correctOption != null && correctOption.matches("[ABCD]");
    }

    public String getFormattedOptions() {
        return String.format("A) %s\nB) %s\nC) %s\nD) %s",
                optionA, optionB, optionC, optionD);
    }
}