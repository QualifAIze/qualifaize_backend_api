package org.qualifaizebackendapi.repository;

import org.qualifaizebackendapi.DTO.QuestionDetailsDTO;
import org.qualifaizebackendapi.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QuestionRepository extends JpaRepository<Question, UUID> {

    @Query("SELECT COUNT(q) FROM Question q WHERE q.interview.id = :interviewId")
    long countByInterviewId(@Param("interviewId") UUID interviewId);

    @Query("""
            SELECT new org.qualifaizebackendapi.DTO.QuestionDetailsDTO(
                q.questionText, q.correctOption, q.submittedAnswer, q.createdAt, q.answeredAt, q.difficulty)
            FROM Question q
            WHERE q.interview.id = :interviewId
            AND q.submittedAnswer IS NOT NULL
            ORDER BY q.questionOrder ASC
            """)
    List<QuestionDetailsDTO> findQuestionsDetailsByInterviewId(UUID interviewId);
}