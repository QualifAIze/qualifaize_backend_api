package org.qualifaizebackendapi.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.aspectj.weaver.patterns.TypePatternQuestions;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.qualifaizebackendapi.model.enums.Difficulty;
import org.qualifaizebackendapi.model.enums.InterviewStatus;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "interview")
public class Interview {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @Size(min = 1, max = 255, message = "Interview name must be between 1 and 255 characters")
    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Difficulty difficulty = Difficulty.MEDIUM;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InterviewStatus status = InterviewStatus.SCHEDULED;

    @Column(name = "scheduled_date")
    private OffsetDateTime scheduledDate;

    @Column(name = "start_time")
    private OffsetDateTime startTime;

    @Column(name = "end_time")
    private OffsetDateTime endTime;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdByUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_user_id")
    private User assignedToUser;

    @OneToMany(mappedBy = "interview", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Question> questions = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public int getQuestionCount() {
        return questions.size();
    }

    public boolean isActive() {
        return InterviewStatus.IN_PROGRESS.equals(this.status);
    }

    public boolean isCompleted() {
        return InterviewStatus.COMPLETED.equals(this.status);
    }

    public String getDocumentTitle() {
        return document != null ? document.getSecondaryFileName() : null;
    }

    public Long getDurationInMinutes() {
        if (startTime != null && endTime != null) {
            return java.time.Duration.between(startTime, endTime).toMinutes();
        }
        return null;
    }

    public void start() {
        this.status = InterviewStatus.IN_PROGRESS;
        this.startTime = OffsetDateTime.now();
    }

    public void complete() {
        this.status = InterviewStatus.COMPLETED;
        this.endTime = OffsetDateTime.now();
    }

    public void cancel() {
        this.status = InterviewStatus.CANCELLED;
    }
}
