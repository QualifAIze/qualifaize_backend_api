package org.qualifaizebackendapi.service.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.mistralai.MistralAiChatModel;
import org.springframework.ai.mistralai.MistralAiChatOptions;
import org.springframework.ai.mistralai.api.MistralAiApi;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class AIClientFactory {

    private final OpenAiChatOptions baseOpenAiOptions;
    private final MistralAiChatOptions baseMistralOptions;

    @Value("${spring.ai.openai.api-key}")
    private String openAiApiKey;

    @Value("${spring.ai.mistralai.api-key}")
    private String mistralAiApiKey;

    @Value("classpath:prompts/content_selection/sectionSelectionSystemPrompt.st")
    private Resource contentSelectionSysPrompt;

    @Value("classpath:prompts/question_generation/generateQuestionSystemPrompt.st")
    private Resource generateQuestionSystemPrompt;

    private final Map<String, ChatClient> clientCache = new ConcurrentHashMap<>();

    public ChatClient createContentSelectionClient(OpenAiApi.ChatModel chatModel) {
        return this.createClient(chatModel, contentSelectionSysPrompt);
    }

    public ChatClient createContentSelectionClient(MistralAiApi.ChatModel chatModel) {
        return this.createClient(chatModel, contentSelectionSysPrompt);
    }

    public ChatClient createQuestionGenerationClient(OpenAiApi.ChatModel chatModel) {
        return this.createClient(chatModel, generateQuestionSystemPrompt);
    }

    public ChatClient createQuestionGenerationClient(MistralAiApi.ChatModel chatModel) {
        return this.createClient(chatModel, generateQuestionSystemPrompt);
    }

    private ChatClient createClient(OpenAiApi.ChatModel model, Resource promptName) {
        String cacheKey = "openai:" + model.getValue() + ":" + promptName.getFilename();

        return clientCache.computeIfAbsent(cacheKey, key -> {
            OpenAiChatOptions options = baseOpenAiOptions.copy();
            options.setModel(model.getValue());

            OpenAiChatModel chatModel = OpenAiChatModel.builder()
                    .defaultOptions(options)
                    .openAiApi(OpenAiApi.builder().apiKey(openAiApiKey).build())
                    .build();

            return ChatClient.builder(chatModel)
                    .defaultSystem(promptName)
                    .build();
        });
    }

    private ChatClient createClient(MistralAiApi.ChatModel model, Resource promptName) {
        String cacheKey = "mistral:" + model.getValue() + ":" + promptName.getFilename();

        return clientCache.computeIfAbsent(cacheKey, key -> {
            MistralAiChatOptions options = baseMistralOptions.copy();
            options.setModel(model.getValue());

            MistralAiChatModel chatModel = MistralAiChatModel.builder()
                    .defaultOptions(options)
                    .mistralAiApi(new MistralAiApi(mistralAiApiKey))
                    .build();

            return ChatClient.builder(chatModel)
                    .defaultSystem(promptName)
                    .build();
        });
    }


}