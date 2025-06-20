package org.qualifaizebackendapi.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.qualifaizebackendapi.DTO.request.interview.CreateInterviewRequest;
import org.qualifaizebackendapi.DTO.response.interview.question.GenerateQuestionDTO;
import org.qualifaizebackendapi.DTO.response.interview.question.QuestionSectionResponse;
import org.qualifaizebackendapi.DTO.response.interview.ChangeInterviewStatusResponse;
import org.qualifaizebackendapi.DTO.response.interview.CreateInterviewResponse;
import org.qualifaizebackendapi.DTO.response.interview.question.QuestionToAsk;
import org.qualifaizebackendapi.DTO.response.interview.question.SubmitAnswerResponse;
import org.qualifaizebackendapi.DTO.response.pdf.UploadedPdfResponseWithConcatenatedContent;
import org.qualifaizebackendapi.DTO.response.pdf.table_of_contents_response.UploadedPdfResponseWithToc;
import org.qualifaizebackendapi.exception.ResourceNotFoundException;
import org.qualifaizebackendapi.mapper.InterviewMapper;
import org.qualifaizebackendapi.model.Document;
import org.qualifaizebackendapi.model.Interview;
import org.qualifaizebackendapi.model.User;
import org.qualifaizebackendapi.model.enums.Difficulty;
import org.qualifaizebackendapi.model.enums.InterviewStatus;
import org.qualifaizebackendapi.repository.InterviewRepository;
import org.qualifaizebackendapi.service.InterviewService;
import org.qualifaizebackendapi.service.PdfService;
import org.qualifaizebackendapi.service.UserService;
import org.qualifaizebackendapi.service.factory.AIClientFactory;
import org.qualifaizebackendapi.utils.SecurityUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class InterviewServiceImpl implements InterviewService {

    private final PdfService pdfService;
    private final UserService userService;
    private final InterviewRepository interviewRepository;
    private final InterviewMapper interviewMapper;
    private final AIClientFactory aiClientFactory;

    @Value("classpath:prompts/content_selection/sectionSelectionUserPrompt.st")
    private Resource contentSelectionUserPrompt;

    @Value("classpath:prompts/question_generation/generateQuestionUserPrompt.st")
    private Resource questionGenerationUserPrompt;

    @Override
    public CreateInterviewResponse createInterview(CreateInterviewRequest request) {
        Document documentToCreateTheInterview = pdfService.findDocumentByIdOrThrow(request.getDocumentId());
        User assignedToUser = userService.fetchUserOrThrow(request.getAssignedToUserId());
        User createdByUser = SecurityUtils.getCurrentUser();

        Interview interviewToSave = interviewMapper.toInterviewFromCreateInterviewRequest(request, documentToCreateTheInterview, assignedToUser, createdByUser);
        return new CreateInterviewResponse(interviewRepository.save(interviewToSave).getId());
    }

    @Override
    public ChangeInterviewStatusResponse updateInterviewStatus(UUID interviewId, InterviewStatus newStatus) {
        Interview interviewToUpdate = this.fetchInterviewOrThrow(interviewId);
        interviewToUpdate.setStatus(newStatus);
        Interview updatedInterview = interviewRepository.save(interviewToUpdate);
        return new ChangeInterviewStatusResponse(updatedInterview.getId(), updatedInterview.getStatus());
    }

    @Override
    public QuestionToAsk getNextInterviewQuestion(UUID interviewId) {
        QuestionSectionResponse questionSectionResponse = this.chooseSectionToGenerateQuestion(interviewId);
        GenerateQuestionDTO generatedQuestion = this.generateNextQuestion(interviewId, questionSectionResponse);
        return null;
    }

    @Override
    public SubmitAnswerResponse submitAnswer(UUID questionId, char userAnswer) {
        return null;
    }

    public Interview fetchInterviewOrThrow(UUID interviewId) {
        return interviewRepository.findById(interviewId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Interview with Id %s was now found!", interviewId)
                ));
    }

    private QuestionSectionResponse chooseSectionToGenerateQuestion(UUID interviewId) {
        ChatClient client = aiClientFactory.createContentSelectionClient(OpenAiApi.ChatModel.GPT_4_1_NANO);
        try {
            return client
                    .prompt()
                    .user(u -> u.text(contentSelectionUserPrompt).params(this.collectContentSelectionPromptParams(interviewId)))
                    .call()
                    .entity(QuestionSectionResponse.class);
        } catch (Exception e) {
            log.error("Error while choosing section!", e);
            throw e;
        } finally {
            log.info("AI content selection process completed");
        }
    }

    private GenerateQuestionDTO generateQuestion(UUID interviewId, String contentToGenerateQuestion) {
        ChatClient client = aiClientFactory.createQuestionGenerationClient(OpenAiApi.ChatModel.GPT_4_O);
        try {
            return client
                    .prompt()
                    .user(u -> u.text(questionGenerationUserPrompt).params(this
                            .collectQuestionGenerationPromptParams(interviewId, contentToGenerateQuestion)))
                    .call()
                    .entity(GenerateQuestionDTO.class);
        } catch (Exception e) {
            log.error("Error while generating question!", e);
            throw e;
        } finally {
            log.info("AI question generation process completed");
        }
    }

    private GenerateQuestionDTO generateNextQuestion(UUID interviewId, QuestionSectionResponse chosenSectionToGenerateQuestion) {
        UUID documentId = interviewRepository.findDocumentIdByInterviewId(interviewId);
        UploadedPdfResponseWithConcatenatedContent contentOfChosenSection = pdfService
                .getConcatenatedContentById(documentId, chosenSectionToGenerateQuestion.getTitle());

        GenerateQuestionDTO generatedQuestion = this.generateQuestion(interviewId, contentOfChosenSection.getContent());
        return null;
    }

    private Map<String, Object> collectContentSelectionPromptParams(UUID interviewId) {
        UUID documentId = this.interviewRepository.findDocumentIdByInterviewId(interviewId);
        UploadedPdfResponseWithToc documentTocDetails = pdfService.getDocumentDetailsAndTocById(documentId);
        List<String> previouslyAskedQuestions = this.interviewRepository.findQuestionTitlesByInterviewId(interviewId);
        String previouslyAskedQuestionsString = this.createTextForAnsweredQuestions(previouslyAskedQuestions);
        log.info("Creating prompt for section selection");
        return Map.of("table_of_contents", documentTocDetails, "answered_questions", previouslyAskedQuestionsString);
    }

    private Map<String, Object> collectQuestionGenerationPromptParams(UUID interviewId, String contentToGenerateQuestion) {
        List<String> previouslyAskedQuestions = this.interviewRepository.findQuestionTitlesByInterviewId(interviewId);
        String previouslyAskedQuestionsString = this.createTextForAnsweredQuestions(previouslyAskedQuestions);
        Interview interview = this.fetchInterviewOrThrow(interviewId);
        log.info("Creating prompt for question generation");
        return Map.of("answered_questions", previouslyAskedQuestionsString, "content", contentToGenerateQuestion,  "difficulty", interview.getDifficulty());
    }

    private String createTextForAnsweredQuestions(List<String> answeredQuestions) {
        if (answeredQuestions == null || answeredQuestions.isEmpty()) {
            return "No previously asked questions";
        }

        StringBuilder sb = new StringBuilder();

        if (answeredQuestions.size() == 1) {
            sb.append("Previously asked question: ").append(answeredQuestions.getFirst());
        } else {
            sb.append("Previously asked questions: ");

            for (int i = 0; i < answeredQuestions.size(); i++) {
                if (i > 0) {
                    if (i == answeredQuestions.size() - 1) {
                        sb.append(" and ");
                    } else {
                        sb.append(", ");
                    }
                }
                sb.append(answeredQuestions.get(i));
            }
        }

        return sb.toString();
    }
}
