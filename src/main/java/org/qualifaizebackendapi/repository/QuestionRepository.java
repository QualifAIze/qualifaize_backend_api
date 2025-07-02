package org.qualifaizebackendapi.repository;

import org.qualifaizebackendapi.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QuestionRepository extends JpaRepository<Question, UUID> {

    @Query("""
                SELECT q FROM Question q
                WHERE q.interview.id = :interviewId
            """)
    List<Question> findQuestionsByInterviewId(@Param("interviewId") UUID interviewId);

    @Query("SELECT COUNT(q) FROM Question q WHERE q.interview.id = :interviewId")
    long countByInterviewId(@Param("interviewId") UUID interviewId);
}