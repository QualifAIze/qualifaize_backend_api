package org.qualifaizebackendapi.service;

import org.qualifaizebackendapi.DTO.request.interview.CreateInterviewRequest;
import org.qualifaizebackendapi.DTO.response.interview.ChangeInterviewStatusResponse;
import org.qualifaizebackendapi.DTO.response.interview.CreateInterviewResponse;
import org.qualifaizebackendapi.exception.ResourceNotFoundException;
import org.qualifaizebackendapi.exception.DuplicateInterviewException;
import org.qualifaizebackendapi.model.enums.InterviewStatus;

import java.util.UUID;

/**
 * Service interface for managing interview operations.
 * Provides methods for creating and managing interviews.
 */
public interface InterviewService {

    /**
     * Creates a new interview based on the provided request.
     *
     * @param request The interview creation request containing all necessary data
     * @return UUID of the newly created interview
     * @throws ResourceNotFoundException if the referenced document or assigned user doesn't exist
     * @throws IllegalArgumentException if the request contains invalid data
     * @throws DuplicateInterviewException if an interview with the same name already exists for the document
     */
    CreateInterviewResponse createInterview(CreateInterviewRequest request);

    ChangeInterviewStatusResponse updateInterviewStatus(UUID interviewId, InterviewStatus newStatus);
}