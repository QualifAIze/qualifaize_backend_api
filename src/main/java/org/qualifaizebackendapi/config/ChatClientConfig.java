package org.qualifaizebackendapi.config;

import org.qualifaizebackendapi.service.factory.AIClientFactory;
import org.springframework.ai.mistralai.MistralAiChatOptions;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

    @Bean
    public AIClientFactory aiClientFactory() {
        return new AIClientFactory(baseOpenAiOptions(), baseMistralOptions());
    }

    @Bean
    public OpenAiChatOptions baseOpenAiOptions() {
        return OpenAiChatOptions.builder()
                .temperature(0.7)
                .topP(0.9)
                .frequencyPenalty(0.5)
                .presencePenalty(0.5)
                .build();
    }

    @Bean
    public MistralAiChatOptions baseMistralOptions() {
        return MistralAiChatOptions.builder()
                .temperature(0.7)
                .frequencyPenalty(0.5)
                .presencePenalty(0.5)
                .topP(0.9)
                .build();
    }
}