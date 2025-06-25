package org.qualifaizebackendapi.service.impl;

import lombok.RequiredArgsConstructor;
import org.qualifaizebackendapi.DTO.response.interview.InterviewDetailsResponse;
import org.qualifaizebackendapi.DTO.response.interview.question.QuestionDetailsResponse;
import org.qualifaizebackendapi.service.AiInterviewReviewService;
import org.qualifaizebackendapi.service.factory.AIClientFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class AiInterviewReviewServiceImpl implements AiInterviewReviewService {

    private final AIClientFactory aiClientFactory;

    @Value("classpath:prompts/interview_review/interviewReviewUserPrompt.st")
    private Resource interviewReviewUserPrompt;

    @Override
    public String reviewInterview(InterviewDetailsResponse interviewDetails) {
        ChatClient interviewReviewClient = aiClientFactory.createInterviewReviewClient(
                OpenAiApi.ChatModel.GPT_4_1
        );

        Map<String, Object> promptParams = this.buildInterviewReviewPromptParameters(interviewDetails);

        String review = Objects.requireNonNull(interviewReviewClient
                .prompt()
                .user(userSpec -> userSpec.text(interviewReviewUserPrompt).params(promptParams))
                .call()
                .chatResponse()).getResult().getOutput().getText();

        return review.replaceAll("(?s)```markdown.*?```", "");
    }

    private Map<String, Object> buildInterviewReviewPromptParameters(InterviewDetailsResponse interviewDetails) {
        String interviewName =  interviewDetails.getName();
        String documentTitle = interviewDetails.getDocumentTitle();
        String interviewDifficulty = interviewDetails.getDifficulty().toString();
        int totalQuestions = interviewDetails.getQuestions().size();
        long totalDurationInSeconds = interviewDetails.getDurationInSeconds() / 60;
        String candidateFirstName = interviewDetails.getAssignedTo().getFirstName();
        String candidateLastName = interviewDetails.getAssignedTo().getLastName();
        String candidateName = String.format("%s %s", candidateFirstName, candidateLastName);
        String answeredQuestions = prepareAnsweredQuestionsAsString(interviewDetails.getQuestions());

        return Map.of(
                "interview_name", interviewName,
                "document_title", documentTitle,
                "interview_difficulty", interviewDifficulty,
                "total_questions", totalQuestions,
                "interview_duration", totalDurationInSeconds,
                "candidate_name", candidateName,
                "questions_analysis", answeredQuestions
        );
    }

    private String prepareAnsweredQuestionsAsString(List<QuestionDetailsResponse> questions) {
        if (questions == null || questions.isEmpty()) {
            return "No questions were answered in this interview.";
        }

        StringBuilder analysisBuilder = new StringBuilder();

        List<QuestionDetailsResponse> answeredQuestions = questions.stream()
                .filter(q -> q.getSubmittedAnswer() != null)
                .sorted(Comparator.comparing(QuestionDetailsResponse::getQuestionOrder))
                .toList();

        if (answeredQuestions.isEmpty()) {
            return "No questions were answered in this interview.";
        }

        for (QuestionDetailsResponse question : answeredQuestions) {
            analysisBuilder.append("Question ").append(question.getQuestionOrder()).append(":\n");
            analysisBuilder.append("Text: ").append(question.getQuestionText()).append("\n");
            analysisBuilder.append("Difficulty: ").append(question.getDifficulty()).append("\n");

            analysisBuilder.append("Options:\n");
            analysisBuilder.append("  A) ").append(question.getOptionA()).append("\n");
            analysisBuilder.append("  B) ").append(question.getOptionB()).append("\n");
            analysisBuilder.append("  C) ").append(question.getOptionC()).append("\n");
            analysisBuilder.append("  D) ").append(question.getOptionD()).append("\n");

            analysisBuilder.append("Candidate's Answer: ").append(question.getSubmittedAnswer()).append("\n");
            analysisBuilder.append("Correct Answer: ").append(question.getCorrectOption()).append("\n");
            analysisBuilder.append("Result: ").append(Boolean.TRUE.equals(question.getIsCorrect()) ? "CORRECT" : "INCORRECT").append("\n");

            if (question.getAnswerTimeInMillis() != null) {
                double timeInSeconds = question.getAnswerTimeInMillis() / 1000.0;
                analysisBuilder.append("Response Time: ").append(String.format("%.1f", timeInSeconds)).append(" seconds\n");
            }

            analysisBuilder.append("\n");
        }

        return analysisBuilder.toString();
    }
}
