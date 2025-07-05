package org.qualifaizebackendapi.service.impl;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.qualifaizebackendapi.DTO.QuestionDetailsDTO;
import org.qualifaizebackendapi.DTO.request.interview.CreateInterviewRequest;
import org.qualifaizebackendapi.DTO.response.interview.AssignedInterviewResponse;
import org.qualifaizebackendapi.DTO.response.interview.InterviewDetailsResponse;
import org.qualifaizebackendapi.DTO.response.interview.ChangeInterviewStatusResponse;
import org.qualifaizebackendapi.DTO.response.interview.CreateInterviewResponse;
import org.qualifaizebackendapi.DTO.response.interview.question.QuestionDetailsResponse;
import org.qualifaizebackendapi.DTO.response.interview.question.QuestionToAsk;
import org.qualifaizebackendapi.DTO.response.interview.question.SubmitAnswerResponse;
import org.qualifaizebackendapi.exception.ResourceNotFoundException;
import org.qualifaizebackendapi.mapper.InterviewMapper;
import org.qualifaizebackendapi.mapper.QuestionMapper;
import org.qualifaizebackendapi.model.Document;
import org.qualifaizebackendapi.model.Interview;
import org.qualifaizebackendapi.model.Question;
import org.qualifaizebackendapi.model.User;
import org.qualifaizebackendapi.model.enums.InterviewStatus;
import org.qualifaizebackendapi.model.enums.Role;
import org.qualifaizebackendapi.repository.InterviewRepository;
import org.qualifaizebackendapi.repository.QuestionRepository;
import org.qualifaizebackendapi.service.*;
import org.qualifaizebackendapi.utils.InterviewProgressCalculator;
import org.qualifaizebackendapi.utils.SecurityUtils;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class InterviewServiceImpl implements InterviewService {

    private final PdfService pdfService;
    private final UserService userService;
    private final QuestionService questionService;

    private final AsyncInterviewReviewService asyncInterviewReviewService;

    private final EntityManager em;

    private final InterviewMapper interviewMapper;
    private final QuestionMapper questionMapper;

    private final InterviewRepository interviewRepository;
    private final QuestionRepository questionRepository;

    @Override
    public CreateInterviewResponse createInterview(CreateInterviewRequest request) {
        log.info("Creating interview '{}' for document: {}", request.getName(), request.getDocumentId());

        Document targetDocument = pdfService.findDocumentByIdOrThrow(request.getDocumentId());
        User assignedUser = userService.fetchUserOrThrow(request.getAssignedToUserId());
        User creatorUser = SecurityUtils.getCurrentUser();

        Interview newInterview = interviewMapper.toInterviewFromCreateInterviewRequest(
                request, targetDocument, creatorUser, assignedUser
        );

        Interview savedInterview = interviewRepository.save(newInterview);

        log.info("Successfully created interview with ID: {} and name: '{}'",
                savedInterview.getId(), savedInterview.getName());

        return new CreateInterviewResponse(savedInterview.getId());
    }

    @Override
    public List<AssignedInterviewResponse> getAssignedInterviews(InterviewStatus status) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        log.info("Fetching assigned interviews for user: {} with status filter: {}", currentUserId, status);

        List<Interview> assignedInterviews = (status != null)
                ? interviewRepository.findInterviewsAssignedToUserByStatus(currentUserId, status)
                : interviewRepository.findInterviewsAssignedToUser(currentUserId);

        log.info("Found {} assigned interviews for user: {}", assignedInterviews.size(), currentUserId);

        return interviewMapper.toAssignedInterviewResponses(assignedInterviews);
    }

    @Override
    public List<InterviewDetailsResponse> getInterviewsWithQuestions(UUID interviewId) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        boolean isAdmin = SecurityUtils.hasRole(Role.ADMIN);

        log.info("Fetching interviews with questions for user: {} (Admin: {}), specific interview: {}",
                currentUserId, isAdmin, interviewId);

        List<Interview> interviews;

        if (interviewId == null) {
            interviews = isAdmin
                    ? interviewRepository.findAll()
                    : interviewRepository.findInterviewsAssignedToUser(currentUserId);
        } else {
            interviews = List.of(fetchInterviewOrThrow(interviewId));
        }

        log.info("Retrieved {} interview(s) with questions", interviews.size());

        List<InterviewDetailsResponse> response = interviewMapper.toInterviewDetailsResponses(interviews);
        response.forEach(interview -> interview.getQuestions()
                .sort(Comparator.comparing(QuestionDetailsResponse::getQuestionOrder)));

        return response;
    }

    @Override
    public ChangeInterviewStatusResponse updateInterviewStatus(UUID interviewId, InterviewStatus newStatus) {
        log.info("Updating interview {} status to: {}", interviewId, newStatus);

        Interview interview = fetchInterviewOrThrow(interviewId);
        interview.setStatus(newStatus);
        Interview updatedInterview = interviewRepository.saveAndFlush(interview);

        log.info("Successfully updated interview {} status to: {}", interviewId, newStatus);

        return new ChangeInterviewStatusResponse(updatedInterview.getId(), updatedInterview.getStatus());
    }

    @Override
    public QuestionToAsk getNextInterviewQuestion(UUID interviewId) {
        log.info("Generating next question for interview: {}", interviewId);

        Interview interview = fetchInterviewOrThrow(interviewId);
        Question nextQuestion = questionService.getNextQuestion(interview);

        log.info("Successfully generated and saved question {} for interview: {}", nextQuestion.getId(), interviewId);

        return questionMapper.toQuestionToAsk(nextQuestion);
    }

    @Override
    @Transactional
    public SubmitAnswerResponse submitAnswer(UUID questionId, String userAnswer) {
        log.info("Processing answer submission for question: {} with answer: {}", questionId, userAnswer);

        Question question = questionService.addUserSubmitAnswer(questionId, userAnswer);

        SubmitAnswerResponse response = questionMapper.toSubmitAnswerResponse(question, userAnswer);
        response.setCorrect(question.isSubmittedAnswerCorrect());

        int interviewProgress = calculateInterviewProgress(question.getInterview());
        response.setCurrentProgress(interviewProgress);

        if (interviewProgress >= 100) completeInterviewAndGenerateReview(questionId);

        log.info("Answer submitted for question {}: {} (Correct: {})", questionId, userAnswer, response.isCorrect());

        return response;
    }

    private void completeInterviewAndGenerateReview(UUID questionId) {
        Interview interview = this.interviewRepository.findInterviewByQuestionId(questionId);

        ChangeInterviewStatusResponse newStatus = this.updateInterviewStatus(interview.getId(), InterviewStatus.COMPLETED);
        interview.setStatus(newStatus.getInterviewStatus());

        InterviewDetailsResponse interviewDetails = this.getInterviewsWithQuestions(interview.getId())
                .stream()
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Interview not found"));

        log.info("Interview completed, triggering async review generation for interview: {}", interview.getId());
        asyncInterviewReviewService.generateAndSaveReview(interview, interviewDetails);
    }

    private Integer calculateInterviewProgress(Interview interview) {
        List<QuestionDetailsDTO> answeredQuestions = questionRepository
                .findQuestionsDetailsByInterviewId(interview.getId());

        return InterviewProgressCalculator.calculateProgress(answeredQuestions);
    }

    private Interview fetchInterviewOrThrow(UUID interviewId) {
        em.clear();
        return interviewRepository.findById(interviewId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Interview with ID %s not found", interviewId)
                ));
    }
}