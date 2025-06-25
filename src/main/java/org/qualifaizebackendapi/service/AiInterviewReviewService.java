package org.qualifaizebackendapi.service;

import org.qualifaizebackendapi.DTO.response.interview.InterviewDetailsResponse;

import java.util.UUID;

public interface AiInterviewReviewService {
    String reviewInterview(InterviewDetailsResponse interviewDetails);
}
