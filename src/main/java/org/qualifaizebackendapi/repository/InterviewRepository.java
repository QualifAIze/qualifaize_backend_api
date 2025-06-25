package org.qualifaizebackendapi.repository;

import org.qualifaizebackendapi.DTO.db_object.QuestionHistoryRow;
import org.qualifaizebackendapi.model.Interview;
import org.qualifaizebackendapi.model.enums.InterviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InterviewRepository extends JpaRepository<Interview, UUID> {

    @Query("""
            SELECT new org.qualifaizebackendapi.DTO.db_object.QuestionHistoryRow(
                q.questionText, q.correctOption, q.submittedAnswer, q.createdAt, q.answeredAt, q.difficulty)
            FROM Question q
            WHERE q.interview.id = :interviewId
            AND q.submittedAnswer IS NOT NULL
            ORDER BY q.questionOrder ASC
            """)
    List<QuestionHistoryRow> findAnsweredQuestionsBasicDataByInterviewId(UUID interviewId);

    @Query("""
            SELECT i.document.id FROM Interview i
                WHERE i.id = :interviewId
            """)
    UUID findDocumentIdByInterviewId(UUID interviewId);

    @Query("""
            SELECT q.questionText FROM Question q
                WHERE q.interview.id = :interviewId
            """)
    List<String> findQuestionTitlesByInterviewId(UUID interviewId);

    @Query("""
            SELECT i FROM Interview i
            LEFT JOIN FETCH i.document d
            LEFT JOIN FETCH i.createdByUser c
            LEFT JOIN FETCH i.questions q
            WHERE i.assignedToUser.id = :userId
            ORDER BY i.createdAt DESC
            """)
    List<Interview> findInterviewsAssignedToUser(@Param("userId") UUID userId);

    @Query("""
            SELECT i FROM Interview i
            LEFT JOIN FETCH i.document d
            LEFT JOIN FETCH i.createdByUser c
            LEFT JOIN FETCH i.questions q
            WHERE i.assignedToUser.id = :userId
            AND i.status = :status
            ORDER BY i.createdAt DESC
            """)
    List<Interview> findInterviewsAssignedToUserByStatus(@Param("userId") UUID userId, @Param("status") InterviewStatus status);

    @Query("""
            SELECT DISTINCT i FROM Interview i
            LEFT JOIN FETCH i.document d
            LEFT JOIN FETCH i.createdByUser c
            LEFT JOIN FETCH i.assignedToUser a
            LEFT JOIN FETCH i.questions q
            WHERE (:userId IS NULL OR i.assignedToUser.id = :userId)
            ORDER BY i.createdAt DESC
            """)
    List<Interview> findInterviewsWithQuestions(@Param("userId") UUID userId);

    @Query("""
            SELECT DISTINCT i FROM Interview i
            LEFT JOIN FETCH i.document d
            LEFT JOIN FETCH i.createdByUser c
            LEFT JOIN FETCH i.assignedToUser a
            LEFT JOIN FETCH i.questions q
            WHERE i.id = :interviewId
            AND (:userId IS NULL OR i.assignedToUser.id = :userId)
            """)
    Optional<Interview> findInterviewWithQuestionsById(@Param("interviewId") UUID interviewId, @Param("userId") UUID userId);

    @Query("""
            SELECT DISTINCT i FROM Interview i
            LEFT JOIN FETCH i.questions q
            WHERE q.id = :questionId
            """)
    Interview findInterviewByQuestionId(UUID questionId);
}
