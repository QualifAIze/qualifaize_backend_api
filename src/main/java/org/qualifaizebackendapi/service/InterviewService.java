package org.qualifaizebackendapi.service;

import org.qualifaizebackendapi.DTO.request.interview.CreateInterviewRequest;
import org.qualifaizebackendapi.DTO.response.interview.AssignedInterviewResponse;
import org.qualifaizebackendapi.DTO.response.interview.ChangeInterviewStatusResponse;
import org.qualifaizebackendapi.DTO.response.interview.CreateInterviewResponse;
import org.qualifaizebackendapi.DTO.response.interview.InterviewDetailsResponse;
import org.qualifaizebackendapi.DTO.response.interview.question.QuestionToAsk;
import org.qualifaizebackendapi.DTO.response.interview.question.SubmitAnswerResponse;
import org.qualifaizebackendapi.exception.ResourceNotFoundException;
import org.qualifaizebackendapi.exception.DuplicateInterviewException;
import org.qualifaizebackendapi.model.enums.InterviewStatus;

import java.util.List;
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

    /**
     * Retrieves interviews with their questions based on user role.
     * Admins see all interviews, regular users see only assigned interviews.
     * If interviewId is provided, returns specific interview, otherwise returns all accessible interviews.
     *
     * @param interviewId Optional UUID of specific interview to retrieve
     * @return List of interviews with complete question details (single item if interviewId provided)
     * @throws ResourceNotFoundException if specific interview not found or access denied
     */
    List<InterviewDetailsResponse> getInterviewsWithQuestions(UUID interviewId);

    /**
     * Retrieves all interviews assigned to the current authenticated user.
     *
     * @param status Optional status filter - if provided, only interviews with this status will be returned
     * @return List of interviews assigned to the current user
     */
    List<AssignedInterviewResponse> getAssignedInterviews(InterviewStatus status);

    /**
     * Updates the status of an existing interview.
     *
     * @param interviewId The UUID of the interview to update
     * @param newStatus The new status to set for the interview
     * @return Response containing the interview ID and updated status
     * @throws ResourceNotFoundException if the interview doesn't exist
     */
    ChangeInterviewStatusResponse updateInterviewStatus(UUID interviewId, InterviewStatus newStatus);

    /**
     * Gets the next question to ask in the specified interview.
     * This returns the next unanswered question from the interview's question pool.
     *
     * @param interviewId The UUID of the interview to get the next question for
     * @return QuestionToAsk object containing the next question details including questionId
     * @throws ResourceNotFoundException if the interview doesn't exist or no more questions available
     */
    QuestionToAsk getNextInterviewQuestion(UUID interviewId);

    /**
     * Submits an answer for a specific question and returns the result with feedback.
     *
     * @param questionId The UUID of the question being answered
     * @param userAnswer The user's answer (A, B, C, or D)
     * @return SubmitAnswerResponse containing the result and current score
     * @throws ResourceNotFoundException if the question doesn't exist
     * @throws IllegalArgumentException if the answer format is invalid
     */
    SubmitAnswerResponse submitAnswer(UUID questionId, String userAnswer);
}