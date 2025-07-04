package org.qualifaizebackendapi.repository;

import org.qualifaizebackendapi.model.Document;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PdfRepository extends SoftDeletableRepository<Document, UUID> {
    boolean existsBySecondaryFileName(String secondaryFileName);
}
