package org.qualifaizebackendapi.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.qualifaizebackendapi.DTO.db_object.QuestionHistoryRow;
import org.qualifaizebackendapi.DTO.request.interview.CreateInterviewRequest;
import org.qualifaizebackendapi.DTO.response.interview.AssignedInterviewResponse;
import org.qualifaizebackendapi.DTO.response.interview.InterviewDetailsResponse;
import org.qualifaizebackendapi.DTO.response.interview.question.GenerateQuestionDTO;
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
    private final AiInterviewGenerationService aiInterviewGenerationService;
    private final AsyncInterviewReviewService asyncInterviewReviewService;

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

        List<Interview> interviews = (interviewId != null)
                ? List.of(fetchInterviewWithQuestionsById(interviewId))
                : interviewRepository.findInterviewsWithQuestions(currentUserId);

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
        Interview updatedInterview = interviewRepository.save(interview);

        log.info("Successfully updated interview {} status to: {}", interviewId, newStatus);

        return new ChangeInterviewStatusResponse(updatedInterview.getId(), updatedInterview.getStatus());
    }

    @Override
    public QuestionToAsk getNextInterviewQuestion(UUID interviewId) {
        log.info("Generating next question for interview: {}", interviewId);

        Interview interview = fetchInterviewOrThrow(interviewId);

        GenerateQuestionDTO generatedQuestion = aiInterviewGenerationService
                .generateNextInterviewQuestion(interview);

        Question savedQuestion = persistGeneratedQuestion(generatedQuestion, interview);

        log.info("Successfully generated and saved question {} for interview: {}",
                savedQuestion.getId(), interviewId);

        return questionMapper.toQuestionToAsk(savedQuestion);
    }

    @Override
    @Transactional
    public SubmitAnswerResponse submitAnswer(UUID questionId, String userAnswer) {
        log.info("Processing answer submission for question: {} with answer: {}", questionId, userAnswer);

        Question question = fetchQuestionOrThrow(questionId);

        question.submitAnswer(userAnswer);
        Question updatedQuestion = questionRepository.save(question);

        SubmitAnswerResponse response = questionMapper.toSubmitAnswerResponse(updatedQuestion, userAnswer);
        response.setCorrect(updatedQuestion.isSubmittedAnswerCorrect());

        int interviewProgress = calculateInterviewProgress(updatedQuestion.getInterview());
        response.setCurrentProgress(interviewProgress);

        if (interviewProgress >= 100) {
            completeInterviewAndGenerateReview(questionId);
        }

        log.info("Answer submitted for question {}: {} (Correct: {})",
                questionId, userAnswer, response.isCorrect());

        return response;
    }

    private void completeInterviewAndGenerateReview(UUID questionId) {
        Interview interview = this.interviewRepository.findInterviewByQuestionId(questionId);

        log.info("Interview completed, triggering async review generation for interview: {}", interview.getId());

        this.updateInterviewStatus(interview.getId(), InterviewStatus.COMPLETED);

        InterviewDetailsResponse interviewDetails = this.getInterviewsWithQuestions(interview.getId())
                .stream()
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Interview not found"));

        asyncInterviewReviewService.generateAndSaveReview(interview, interviewDetails);
    }

    private Question persistGeneratedQuestion(GenerateQuestionDTO generatedQuestion, Interview interview) {
        Question questionEntity = questionMapper.toQuestion(generatedQuestion, interview);

        long currentQuestionCount = questionRepository.countByInterviewId(interview.getId());
        questionEntity.setQuestionOrder(Math.toIntExact(currentQuestionCount) + 1);

        Question savedQuestion = questionRepository.save(questionEntity);

        log.debug("Persisted question {} with order {} for interview: {}",
                savedQuestion.getId(), savedQuestion.getQuestionOrder(), interview.getId());

        return savedQuestion;
    }

    private Integer calculateInterviewProgress(Interview interview) {
        List<QuestionHistoryRow> answeredQuestions = interviewRepository
                .findAnsweredQuestionsBasicDataByInterviewId(interview.getId());

        return InterviewProgressCalculator.calculateProgress(answeredQuestions);
    }

    private Interview fetchInterviewWithQuestionsById(UUID interviewId) {
        Optional<Interview> interview = interviewRepository.findInterviewById(interviewId);
        if (interview.isPresent()) {
            List<Question> questions = questionRepository.findQuestionsByInterviewId(interviewId);
            interview.get().getQuestions().clear();
            interview.get().getQuestions().addAll(questions);
        }
        return interview.orElseThrow(() -> new ResourceNotFoundException("Interview not found"));
    }

    private Interview fetchInterviewOrThrow(UUID interviewId) {
        return interviewRepository.findById(interviewId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Interview with ID %s not found", interviewId)
                ));
    }

    private Question fetchQuestionOrThrow(UUID questionId) {
        return questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Question with ID %s not found", questionId)
                ));
    }
}