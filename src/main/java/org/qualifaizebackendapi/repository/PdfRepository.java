package org.qualifaizebackendapi.repository;

import org.qualifaizebackendapi.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PdfRepository extends JpaRepository<Document, UUID> {
}
