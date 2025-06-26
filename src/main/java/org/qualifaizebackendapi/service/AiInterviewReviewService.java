package org.qualifaizebackendapi.service;

import org.qualifaizebackendapi.DTO.response.interview.InterviewDetailsResponse;

public interface AiInterviewReviewService {
    String reviewInterview(InterviewDetailsResponse interviewDetails);
}
