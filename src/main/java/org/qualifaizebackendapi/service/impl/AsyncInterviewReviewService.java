package org.qualifaizebackendapi.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.qualifaizebackendapi.DTO.response.interview.InterviewDetailsResponse;
import org.qualifaizebackendapi.model.Interview;
import org.qualifaizebackendapi.repository.InterviewRepository;
import org.qualifaizebackendapi.service.AiInterviewReviewService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class AsyncInterviewReviewService {

    private final AiInterviewReviewService aiInterviewReviewService;
    private final InterviewRepository interviewRepository;

    @Async("interviewReviewExecutor")
    @Transactional
    public void generateAndSaveReview(Interview interview, InterviewDetailsResponse interviewDetails) {
        try {
            log.info("Starting async review generation for interview: {}", interview.getId());

            String review = aiInterviewReviewService.reviewInterview(interviewDetails);

            interview.setCandidateReview(review);
            interviewRepository.save(interview);

            log.info("Successfully generated and saved review for interview: {}", interview.getId());

        } catch (Exception e) {
            log.error("Failed to generate review for interview: {}", interview.getName(), e);
        }

        CompletableFuture.completedFuture(null);
    }
}