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
                .temperature(0.1)
                .frequencyPenalty(0.0)
                .presencePenalty(0.0)
                .topP(1.0)
                .build();
    }

    @Bean
    public MistralAiChatOptions baseMistralOptions() {
        return MistralAiChatOptions.builder()
                .temperature(0.1)
                .frequencyPenalty(0.0)
                .presencePenalty(0.0)
                .topP(1.0)
                .build();
    }
}