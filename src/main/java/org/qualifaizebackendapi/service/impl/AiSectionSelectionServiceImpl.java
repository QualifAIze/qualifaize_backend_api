package org.qualifaizebackendapi.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.qualifaizebackendapi.DTO.response.interview.question.QuestionSectionResponse;
import org.qualifaizebackendapi.model.Document;
import org.qualifaizebackendapi.model.Interview;
import org.qualifaizebackendapi.service.AiSectionSelectionService;
import org.qualifaizebackendapi.service.factory.AIClientFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.mistralai.api.MistralAiApi;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiSectionSelectionServiceImpl implements AiSectionSelectionService {

    private final AIClientFactory aiClientFactory;

    @Value("classpath:prompts/content_selection/sectionSelectionUserPrompt.st")
    private Resource contentSelectionUserPrompt;

    //private final OpenAiApi.ChatModel LLM_MODEL = OpenAiApi.ChatModel.GPT_4_1;
    private final MistralAiApi.ChatModel LLM_MODEL = MistralAiApi.ChatModel.LARGE;

    @Override
    public QuestionSectionResponse selectSectionForNextQuestion(Interview interview, String previousQuestionsAnalysisText) {
        log.info("Starting AI-powered section selection for interview: {}", interview.getId());

        ChatClient contentSelectionClient = aiClientFactory.createContentSelectionClient(LLM_MODEL);

        Document document = interview.getDocument();
        String text = document.getTableOfContentsText();

        Map<String, Object> promptParams = Map.of(
                "table_of_contents", text,
                "answered_questions", previousQuestionsAnalysisText
        );

        QuestionSectionResponse selectedSection = contentSelectionClient
                .prompt()
                .user(userSpec -> userSpec.text(contentSelectionUserPrompt).params(promptParams))
                .call()
                .entity(QuestionSectionResponse.class);

        log.info("AI selected section '{}' for interview: {} - Reason: {}",
                selectedSection.getTitle(), interview.getId(), selectedSection.getExplanation());

        return selectedSection;
    }
}
