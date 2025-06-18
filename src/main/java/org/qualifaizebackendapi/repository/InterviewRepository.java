package org.qualifaizebackendapi.repository;

import org.qualifaizebackendapi.model.Interview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface InterviewRepository extends JpaRepository<Interview, UUID> {
}
