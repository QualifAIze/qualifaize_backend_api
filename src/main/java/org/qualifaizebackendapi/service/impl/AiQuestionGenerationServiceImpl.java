package org.qualifaizebackendapi.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.qualifaizebackendapi.DTO.response.interview.question.GenerateQuestionDTO;
import org.qualifaizebackendapi.DTO.response.interview.question.QuestionSectionResponse;
import org.qualifaizebackendapi.DTO.response.pdf.UploadedPdfResponseWithConcatenatedContent;
import org.qualifaizebackendapi.model.Interview;
import org.qualifaizebackendapi.repository.InterviewRepository;
import org.qualifaizebackendapi.service.AiQuestionGenerationService;
import org.qualifaizebackendapi.service.AiSectionSelectionService;
import org.qualifaizebackendapi.service.PdfService;
import org.qualifaizebackendapi.service.factory.AIClientFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.mistralai.api.MistralAiApi;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiQuestionGenerationServiceImpl implements AiQuestionGenerationService {

    private final AIClientFactory aiClientFactory;
    private final PdfService pdfService;
    private final InterviewRepository interviewRepository;

    private final AiSectionSelectionService aiSectionSelectionService;

    @Value("classpath:prompts/question_generation/generateQuestionUserPrompt.st")
    private Resource questionGenerationUserPrompt;

    //private final OpenAiApi.ChatModel LLM_MODEL = OpenAiApi.ChatModel.GPT_4_1;
    private final MistralAiApi.ChatModel LLM_MODEL = MistralAiApi.ChatModel.LARGE;

    @Override
    public GenerateQuestionDTO generateNextInterviewQuestion(Interview interview, String previousQuestionAnalysisText) {
        log.info("Starting complete question generation process for interview: {}", interview.getId());

        QuestionSectionResponse selectedSection = aiSectionSelectionService.selectSectionForNextQuestion(interview, previousQuestionAnalysisText);
        String sectionContent = retrieveContentForSection(interview, selectedSection.getTitle());
        GenerateQuestionDTO generatedQuestion = generateQuestionFromContent(previousQuestionAnalysisText, interview,
                sectionContent);

        log.info("Completed question generation process for interview: {} - Section: '{}', Question: '{}'",
                interview.getId(), selectedSection.getTitle(), generatedQuestion.getQuestionText());

        return generatedQuestion;
    }

    private GenerateQuestionDTO generateQuestionFromContent(String previousQuestionsAnalysisText, Interview interview,
                                                            String contentForQuestion) {
        ChatClient questionGenerationClient = aiClientFactory.createQuestionGenerationClient(LLM_MODEL);

        Map<String, Object> promptParams = Map.of(
                "answered_questions", previousQuestionsAnalysisText,
                "content", contentForQuestion,
                "difficulty", interview.getDifficulty()
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

    private String retrieveContentForSection(Interview interview, String sectionTitle) {
        UUID documentId = interviewRepository.findDocumentIdByInterviewId(interview.getId());

        UploadedPdfResponseWithConcatenatedContent sectionContent =
                pdfService.getConcatenatedContentById(documentId, sectionTitle);

        return sectionContent.getContent();
    }
}