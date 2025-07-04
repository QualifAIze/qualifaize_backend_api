package org.qualifaizebackendapi.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.qualifaizebackendapi.model.enums.Difficulty;
import org.qualifaizebackendapi.model.enums.InterviewStatus;

import java.time.Duration;
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
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(nullable = false, columnDefinition = "difficulty")
    private Difficulty difficulty = Difficulty.MEDIUM;

    @NotNull
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(nullable = false, columnDefinition = "interview_status")
    private InterviewStatus status = InterviewStatus.SCHEDULED;

    @Column(name = "scheduled_date")
    private OffsetDateTime scheduledDate;

    @Column(name = "start_time")
    private OffsetDateTime startTime;

    @Column(name = "end_time")
    private OffsetDateTime endTime;

    @Column(name = "candidate_review", columnDefinition = "TEXT")
    private String candidateReview;

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

    @OneToMany(mappedBy = "interview", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Question> questions = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public void setStatus(InterviewStatus status) {
        switch (status) {
            case SCHEDULED -> this.schedule();
            case IN_PROGRESS -> this.start();
            case COMPLETED -> this.complete();
            case CANCELLED -> this.cancel();
        }
    }

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

    public Long getDurationInSeconds() {
        if (startTime != null && endTime != null) {
            return Duration.between(startTime, endTime).toSeconds();
        }
        return null;
    }

    public void schedule() {
        if (this.status == InterviewStatus.CANCELLED) {
            this.status = InterviewStatus.SCHEDULED;
        }
    }

    public void start() {
        if (this.status == InterviewStatus.SCHEDULED || this.status == InterviewStatus.CANCELLED) {
            this.status = InterviewStatus.IN_PROGRESS;
            this.startTime = OffsetDateTime.now();
        }
    }

    public void complete() {
            if (this.status == InterviewStatus.IN_PROGRESS) {
            this.status = InterviewStatus.COMPLETED;
            this.endTime = OffsetDateTime.now();
        }
    }

    public void cancel() {
        if (this.status == InterviewStatus.SCHEDULED || this.status == InterviewStatus.IN_PROGRESS) {
            this.status = InterviewStatus.CANCELLED;
        }
    }
}
