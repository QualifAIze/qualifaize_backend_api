package org.qualifaizebackendapi.repository;

import org.qualifaizebackendapi.model.Interview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InterviewRepository extends JpaRepository<Interview, UUID> {

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
}
