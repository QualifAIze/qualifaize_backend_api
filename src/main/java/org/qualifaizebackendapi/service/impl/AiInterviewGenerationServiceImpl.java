package org.qualifaizebackendapi.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.qualifaizebackendapi.DTO.response.interview.question.GenerateQuestionDTO;
import org.qualifaizebackendapi.DTO.response.interview.question.QuestionSectionResponse;
import org.qualifaizebackendapi.DTO.response.pdf.UploadedPdfResponseWithConcatenatedContent;
import org.qualifaizebackendapi.DTO.response.pdf.table_of_contents_response.UploadedPdfResponseWithToc;
import org.qualifaizebackendapi.model.Interview;
import org.qualifaizebackendapi.repository.InterviewRepository;
import org.qualifaizebackendapi.service.AiInterviewGenerationService;
import org.qualifaizebackendapi.service.PdfService;
import org.qualifaizebackendapi.service.factory.AIClientFactory;
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
public class AiInterviewGenerationServiceImpl implements AiInterviewGenerationService {

    private final AIClientFactory aiClientFactory;
    private final PdfService pdfService;
    private final InterviewRepository interviewRepository;

    @Value("classpath:prompts/content_selection/sectionSelectionUserPrompt.st")
    private Resource contentSelectionUserPrompt;

    @Value("classpath:prompts/question_generation/generateQuestionUserPrompt.st")
    private Resource questionGenerationUserPrompt;

    @Override
    public QuestionSectionResponse selectDocumentSectionForQuestion(Interview interview) {
        log.info("Starting AI-powered section selection for interview: {}", interview.getId());

        ChatClient contentSelectionClient = aiClientFactory.createContentSelectionClient(
                OpenAiApi.ChatModel.GPT_4_O
        );

        Map<String, Object> promptParams = buildContentSelectionPromptParameters(interview);

        QuestionSectionResponse selectedSection = contentSelectionClient
                .prompt()
                .user(userSpec -> userSpec.text(contentSelectionUserPrompt).params(promptParams))
                .call()
                .entity(QuestionSectionResponse.class);

        log.info("AI selected section '{}' for interview: {} - Reason: {}",
                selectedSection.getTitle(), interview.getId(), selectedSection.getExplanation());

        return selectedSection;
    }

    @Override
    public GenerateQuestionDTO generateNextInterviewQuestion(Interview interview) {
        log.info("Starting complete question generation process for interview: {}", interview.getId());

        QuestionSectionResponse selectedSection = selectDocumentSectionForQuestion(interview);
        String sectionContent = retrieveContentForSection(interview, selectedSection.getTitle());
        GenerateQuestionDTO generatedQuestion = generateQuestionFromContent(interview, sectionContent);

        log.info("Completed question generation process for interview: {} - Section: '{}', Question: '{}'",
                interview.getId(), selectedSection.getTitle(),
                truncateString(generatedQuestion.getQuestionText(), 50));

        return generatedQuestion;
    }

    private String retrieveContentForSection(Interview interview, String sectionTitle) {
        UUID documentId = interviewRepository.findDocumentIdByInterviewId(interview.getId());

        UploadedPdfResponseWithConcatenatedContent sectionContent =
                pdfService.getConcatenatedContentById(documentId, sectionTitle);

        return sectionContent.getContent();
    }

    private GenerateQuestionDTO generateQuestionFromContent(Interview interview, String contentForQuestion) {
        log.info("Generating question for interview: {} using AI", interview.getId());

        ChatClient questionGenerationClient = aiClientFactory.createQuestionGenerationClient(
                OpenAiApi.ChatModel.GPT_4_O_MINI
        );

        Map<String, Object> promptParams = buildQuestionGenerationPromptParameters(
                interview, contentForQuestion
        );

        GenerateQuestionDTO generatedQuestion = questionGenerationClient
                .prompt()
                .user(userSpec -> userSpec.text(questionGenerationUserPrompt).params(promptParams))
                .call()
                .entity(GenerateQuestionDTO.class);

        log.info("Successfully generated question for interview: {} - Difficulty: {}",
                interview.getId(), generatedQuestion.getDifficulty());

        return generatedQuestion;
    }

    private Map<String, Object> buildContentSelectionPromptParameters(Interview interview) {
        UUID documentId = interviewRepository.findDocumentIdByInterviewId(interview.getId());

        UploadedPdfResponseWithToc documentStructure =
                pdfService.getDocumentDetailsAndTocById(documentId);

        List<String> previousQuestionTitles =
                interviewRepository.findQuestionTitlesByInterviewId(interview.getId());

        String previousQuestionsText = formatPreviousQuestionsForPrompt(previousQuestionTitles);

        log.debug("Content selection parameters - Document: {}, Previous questions: {}",
                documentStructure.getFilename(), previousQuestionTitles.size());

        return Map.of(
                "table_of_contents", documentStructure,
                "answered_questions", previousQuestionsText
        );
    }

    private Map<String, Object> buildQuestionGenerationPromptParameters(
            Interview interview, String contentForQuestion) {

        List<String> previousQuestionTitles =
                interviewRepository.findQuestionTitlesByInterviewId(interview.getId());

        String previousQuestionsText = formatPreviousQuestionsForPrompt(previousQuestionTitles);

        log.debug("Question generation parameters - Content length: {} chars, Difficulty: {}, Previous questions: {}",
                contentForQuestion.length(), interview.getDifficulty(), previousQuestionTitles.size());

        return Map.of(
                "answered_questions", previousQuestionsText,
                "content", contentForQuestion,
                "difficulty", interview.getDifficulty()
        );
    }

    private String formatPreviousQuestionsForPrompt(List<String> previousQuestions) {
        if (previousQuestions == null || previousQuestions.isEmpty()) {
            return "No previously asked questions";
        }

        if (previousQuestions.size() == 1) {
            return "Previously asked question: " + previousQuestions.getFirst();
        }

        StringBuilder formatted = new StringBuilder("Previously asked questions: ");
        for (int i = 0; i < previousQuestions.size(); i++) {
            if (i > 0) {
                formatted.append(i == previousQuestions.size() - 1 ? " and " : ", ");
            }
            formatted.append(previousQuestions.get(i));
        }

        return formatted.toString();
    }

    private String truncateString(String text, int maxLength) {
        if (text == null) return "null";
        return text.length() <= maxLength ? text : text.substring(0, maxLength) + "...";
    }
}