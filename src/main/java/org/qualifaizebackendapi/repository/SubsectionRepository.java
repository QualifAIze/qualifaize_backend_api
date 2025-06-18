package org.qualifaizebackendapi.repository;

import org.qualifaizebackendapi.DTO.db_object.SubsectionRow;
import org.qualifaizebackendapi.model.Subsection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SubsectionRepository extends JpaRepository<Subsection, UUID> {
    @Query("""
              SELECT new org.qualifaizebackendapi.DTO.db_object.SubsectionRow(s.id, s.parent.id, s.title, s.level, s.position)
              FROM Subsection s
              WHERE s.document.id = :documentId
              ORDER BY s.position
            """)
    List<SubsectionRow> fetchFlatToc(@Param("documentId") UUID documentId);
}
