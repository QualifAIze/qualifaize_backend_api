package org.qualifaizebackendapi.repository;

import org.qualifaizebackendapi.DTO.db_object.DocumentWithUserRow;
import org.qualifaizebackendapi.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PdfRepository extends JpaRepository<Document, UUID> {
    boolean existsBySecondaryFileName(String secondaryFileName);

    @Query("""
            SELECT new org.qualifaizebackendapi.DTO.db_object.DocumentWithUserRow(u.id, u.username, u.firstName,
                        u.lastName, d.id, d.fileName, d.secondaryFileName, d.createdAt)
            FROM Document d
            JOIN User u ON d.uploadedByUser.id = u.id
            """)
    List<DocumentWithUserRow> getAllDocumentWithUserDetails();

    @Query("""
            SELECT new org.qualifaizebackendapi.DTO.db_object.DocumentWithUserRow(u.id, u.username, u.firstName,
                        u.lastName, d.id, d.fileName, d.secondaryFileName, d.createdAt)
            FROM Document d
            JOIN User u ON d.uploadedByUser.id = u.id
            WHERE d.id = :documentId
            """)
    Optional<DocumentWithUserRow> getDocumentWithUserDetails(UUID documentId);

    @Query(value = """
            WITH RECURSIVE subsection_descendants AS (
                SELECT
                    s.id,
                    s.document_id,
                    s.parent_id,
                    s.title,
                    s.content,
                    s.position,
                    s.level,
                    s.created_at,
                    0 as depth_level,
                    ARRAY[s.id] as path,
                    d.file_name as document_filename,
                    d.secondary_file_name as document_secondary_filename,
                    d.created_at as document_created_at,
                    s.title as top_section_name,
                    LPAD(s.position::text, 10, '0') as sort_path
                FROM subsection s
                INNER JOIN document d ON s.document_id = d.id
                WHERE s.title = :sectionTitle 
                  AND s.document_id = :documentId
            
                UNION ALL
            
                SELECT
                    s.id,
                    s.document_id,
                    s.parent_id,
                    s.title,
                    s.content,
                    s.position,
                    s.level,
                    s.created_at,
                    sd.depth_level + 1,
                    sd.path || s.id,
                    sd.document_filename,
                    sd.document_secondary_filename,
                    sd.document_created_at,
                    sd.top_section_name,
                    sd.sort_path || '.' || LPAD(s.position::text, 10, '0')
                FROM subsection s
                INNER JOIN subsection_descendants sd ON s.parent_id = sd.id
                WHERE NOT s.id = ANY(sd.path)
            )
            SELECT
                document_filename,
                document_secondary_filename,
                document_created_at,
                document_id,
                top_section_name,
                STRING_AGG(
                    COALESCE(content, ''),
                    ' '
                    ORDER BY sort_path
                ) as combined_content,
                COUNT(*) as total_sections
            FROM subsection_descendants
            WHERE content IS NOT NULL
              AND TRIM(content) != ''
            GROUP BY 
                document_filename,
                document_secondary_filename, 
                document_created_at,
                document_id,
                top_section_name
            """, nativeQuery = true)
    List<Object[]> findSubsectionContentByTitleAndDocumentId(
            @Param("sectionTitle") String sectionTitle,
            @Param("documentId") UUID documentId
    );
}
