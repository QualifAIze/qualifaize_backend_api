package org.qualifaizebackendapi.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.qualifaizebackendapi.DTO.db_object.QuestionHistoryRow;
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
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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
                truncateText(generatedQuestion.getQuestionText(), 50));

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

        String previousQuestionsText = fetchAndFormatPreviousQuestionsForPrompt(interview.getId());

        return Map.of(
                "table_of_contents", documentStructure,
                "answered_questions", previousQuestionsText
        );
    }

    private Map<String, Object> buildQuestionGenerationPromptParameters(
            Interview interview, String contentForQuestion) {

        String previousQuestionsText = fetchAndFormatPreviousQuestionsForPrompt(interview.getId());

        log.debug("Question generation parameters building");

        return Map.of(
                "answered_questions", previousQuestionsText,
                "content", contentForQuestion,
                "difficulty", interview.getDifficulty()
        );
    }

    private String fetchAndFormatPreviousQuestionsForPrompt(UUID interviewId) {
        List<QuestionHistoryRow> previousQuestions = interviewRepository
                .findAnsweredQuestionsBasicDataByInterviewId(interviewId);

        if (previousQuestions == null || previousQuestions.isEmpty()) {
            return "No previously asked questions in this interview. Start with baseline difficulty questions.";
        }

        long correctAnswers = previousQuestions.stream()
                .mapToLong(q -> q.isSubmittedAnswerCorrect() ? 1 : 0)
                .sum();

        double accuracyPercentage = (double) correctAnswers / previousQuestions.size() * 100;
        TimingAnalysis timingAnalysis = analyzeTimingPatterns(previousQuestions);

        StringBuilder prompt = new StringBuilder();

        prompt.append(String.format(
                "INTERVIEW PERFORMANCE: %d questions answered, %.1f%% accuracy (%d correct, %d incorrect). ",
                previousQuestions.size(),
                accuracyPercentage,
                correctAnswers,
                previousQuestions.size() - correctAnswers
        ));

        prompt.append(String.format(
                "TIMING: Average %.1fs per question, %s pace. ",
                timingAnalysis.averageTimeSeconds,
                timingAnalysis.paceDescription
        ));

        prompt.append(getDifficultyGuidance(accuracyPercentage, timingAnalysis));

        if (previousQuestions.size() >= 3) {
            prompt.append("\n\nDIFFICULTY PATTERNS: ");
            addDifficultyPatternAnalysis(prompt, previousQuestions);
        }

        if (previousQuestions.size() >= 3) {
            addPerformanceTrend(prompt, previousQuestions);
        }

        prompt.append("\n\nQUESTION HISTORY:\n");

        for (int i = 0; i < previousQuestions.size(); i++) {
            QuestionHistoryRow question = previousQuestions.get(i);

            prompt.append(String.format("%d. ", i + 1));

            String questionText = truncateText(question.getQuestionText(), 100);
            prompt.append("Q: ").append(questionText);

            String status = question.isSubmittedAnswerCorrect() ? "CORRECT" : "INCORRECT";
            String timingInfo = formatAnswerTime(question.getAnswerTimeInMillis());

            prompt.append(String.format(" | Answer: %s | Correct: %s | Result: %s | Time: %s | Difficulty: %s",
                    question.getSubmittedAnswer(),
                    question.getCorrectOption(),
                    status,
                    timingInfo,
                    question.getDifficulty()
            ));

            if (i < previousQuestions.size() - 1) {
                prompt.append("\n");
            }
        }

        if (previousQuestions.size() >= 4) {
            prompt.append("\n\nANSWER PATTERNS: ");
            addAnswerPatternAnalysis(prompt, previousQuestions);
        }

        if (previousQuestions.size() >= 3) {
            prompt.append("\n\nTIMING PATTERNS: ");
            addTimingPatternAnalysis(prompt, previousQuestions, timingAnalysis);
        }

        return prompt.toString();
    }

    private record TimingAnalysis(double averageTimeSeconds, String paceDescription, boolean isRushed,
                                  boolean isDeliberating) {
    }

    private TimingAnalysis analyzeTimingPatterns(List<QuestionHistoryRow> questions) {
        List<Long> answerTimes = questions.stream()
                .map(QuestionHistoryRow::getAnswerTimeInMillis)
                .filter(time -> time != null && time > 0)
                .toList();

        if (answerTimes.isEmpty()) {
            return new TimingAnalysis(0.0, "unknown", false, false);
        }

        double averageMillis = answerTimes.stream().mapToLong(Long::longValue).average().orElse(0.0);
        double averageSeconds = averageMillis / 1000.0;

        boolean isRushed = averageSeconds < 10;
        boolean isDeliberating = averageSeconds > 120;

        String paceDescription;
        if (isRushed) {
            paceDescription = "very fast (possibly guessing)";
        } else if (isDeliberating) {
            paceDescription = "very slow (thorough consideration)";
        } else if (averageSeconds < 30) {
            paceDescription = "fast (confident)";
        } else if (averageSeconds < 60) {
            paceDescription = "moderate (normal thinking)";
        } else {
            paceDescription = "slow (careful analysis)";
        }

        return new TimingAnalysis(averageSeconds, paceDescription, isRushed, isDeliberating);
    }

    private String getDifficultyGuidance(double accuracyPercentage, TimingAnalysis timing) {
        StringBuilder guidance = new StringBuilder("GUIDANCE: ");

        if (accuracyPercentage >= 80) {
            if (timing.isRushed) {
                guidance.append("Excellent accuracy with fast responses - candidate very confident, increase complexity significantly. ");
            } else if (timing.isDeliberating) {
                guidance.append("Excellent accuracy but slow responses - candidate careful, moderate difficulty increase. ");
            } else {
                guidance.append("Excellent performance with good timing - increase difficulty and complexity. ");
            }
        } else if (accuracyPercentage >= 60) {
            if (timing.isRushed) {
                guidance.append("Good accuracy but rushing - may benefit from slightly harder questions to slow down thinking. ");
            } else {
                guidance.append("Good performance - maintain current difficulty with slight increases. ");
            }
        } else if (accuracyPercentage >= 40) {
            if (timing.isDeliberating) {
                guidance.append("Struggling despite careful consideration - use easier questions with clear explanations. ");
            } else {
                guidance.append("Moderate performance - focus on core concepts, avoid advanced topics. ");
            }
        } else {
            if (timing.isRushed) {
                guidance.append("Poor accuracy with fast responses - candidate likely guessing, use very basic questions. ");
            } else {
                guidance.append("Poor performance - use fundamental questions with detailed explanations. ");
            }
        }

        return guidance.toString();
    }

    private void addPerformanceTrend(StringBuilder prompt, List<QuestionHistoryRow> questions) {
        int recentCount = Math.min(3, questions.size());
        List<QuestionHistoryRow> recentQuestions = questions.subList(
                questions.size() - recentCount, questions.size());

        long recentCorrect = recentQuestions.stream()
                .mapToLong(q -> q.isSubmittedAnswerCorrect() ? 1 : 0)
                .sum();

        double recentAvgTime = recentQuestions.stream()
                .mapToLong(q -> q.getAnswerTimeInMillis() != null ? q.getAnswerTimeInMillis() : 0L)
                .filter(time -> time > 0)
                .average()
                .orElse(0.0) / 1000.0;

        if (recentCorrect == recentCount) {
            prompt.append("TREND: Last ").append(recentCount).append(" answers all correct");
            if (recentAvgTime < 20) {
                prompt.append(" with fast responses - candidate very confident, ready for harder questions. ");
            } else {
                prompt.append(" - candidate improving, ready for moderate difficulty increase. ");
            }
        } else if (recentCorrect == 0) {
            prompt.append("TREND: Last ").append(recentCount).append(" answers all wrong");
            if (recentAvgTime > 60) {
                prompt.append(" despite slow consideration - reduce difficulty significantly. ");
            } else {
                prompt.append(" - candidate struggling, reduce difficulty. ");
            }
        } else {
            prompt.append("TREND: Mixed recent performance (")
                    .append(recentCorrect).append("/").append(recentCount)
                    .append(" correct) with ").append(String.format("%.1fs", recentAvgTime))
                    .append(" average time - maintain current difficulty level. ");
        }
    }

    private void addTimingPatternAnalysis(StringBuilder prompt, List<QuestionHistoryRow> questions, TimingAnalysis overall) {
        List<QuestionHistoryRow> correctAnswers = questions.stream()
                .filter(QuestionHistoryRow::isSubmittedAnswerCorrect)
                .toList();

        List<QuestionHistoryRow> incorrectAnswers = questions.stream()
                .filter(q -> !q.isSubmittedAnswerCorrect())
                .toList();

        if (!correctAnswers.isEmpty() && !incorrectAnswers.isEmpty()) {
            double avgCorrectTime = correctAnswers.stream()
                    .mapToLong(q -> q.getAnswerTimeInMillis() != null ? q.getAnswerTimeInMillis() : 0L)
                    .filter(time -> time > 0)
                    .average()
                    .orElse(0.0) / 1000.0;

            double avgIncorrectTime = incorrectAnswers.stream()
                    .mapToLong(q -> q.getAnswerTimeInMillis() != null ? q.getAnswerTimeInMillis() : 0L)
                    .filter(time -> time > 0)
                    .average()
                    .orElse(0.0) / 1000.0;

            if (avgCorrectTime > 0 && avgIncorrectTime > 0) {
                if (avgCorrectTime < avgIncorrectTime * 0.7) {
                    prompt.append("Correct answers are significantly faster (")
                            .append(String.format("%.1fs vs %.1fs", avgCorrectTime, avgIncorrectTime))
                            .append(") - candidate confident when they know the answer. ");
                } else if (avgIncorrectTime < avgCorrectTime * 0.7) {
                    prompt.append("Incorrect answers are faster (")
                            .append(String.format("%.1fs vs %.1fs", avgIncorrectTime, avgCorrectTime))
                            .append(") - candidate may be guessing when uncertain. ");
                } else {
                    prompt.append("Similar timing for correct and incorrect answers - consistent thinking process. ");
                }
            }
        }

        if (overall.isRushed) {
            prompt.append("Overall very fast responses suggest confidence or impulsiveness. ");
        } else if (overall.isDeliberating) {
            prompt.append("Overall slow responses indicate careful consideration or uncertainty. ");
        }
    }

    private String formatAnswerTime(Long timeInMillis) {
        if (timeInMillis == null || timeInMillis <= 0) {
            return "unknown";
        }

        double seconds = timeInMillis / 1000.0;

        if (seconds < 60) {
            return String.format("%.1fs", seconds);
        } else {
            int minutes = (int) (seconds / 60);
            int remainingSeconds = (int) (seconds % 60);
            return String.format("%dm %ds", minutes, remainingSeconds);
        }
    }

    private void addAnswerPatternAnalysis(StringBuilder prompt, List<QuestionHistoryRow> questions) {
        Map<String, Long> answerCounts = questions.stream()
                .collect(Collectors.groupingBy(
                        QuestionHistoryRow::getSubmittedAnswer,
                        Collectors.counting()
                ));

        Optional<Map.Entry<String, Long>> mostCommon = answerCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue());

        if (mostCommon.isPresent()) {
            String commonAnswer = mostCommon.get().getKey();
            long count = mostCommon.get().getValue();
            double percentage = (double) count / questions.size() * 100;

            if (percentage > 50) {
                prompt.append(String.format("Candidate favors option %s (%.0f%% of answers) - avoid making this the correct answer too often. ",
                        commonAnswer, percentage));
            } else {
                prompt.append("Balanced answer distribution - good variety in question design. ");
            }
        }
    }

    private void addDifficultyPatternAnalysis(StringBuilder prompt, List<QuestionHistoryRow> questions) {
        Map<String, List<QuestionHistoryRow>> questionsByDifficulty = questions.stream()
                .collect(Collectors.groupingBy(q -> q.getDifficulty().toString()));

        for (Map.Entry<String, List<QuestionHistoryRow>> entry : questionsByDifficulty.entrySet()) {
            String difficulty = entry.getKey();
            List<QuestionHistoryRow> difficultyQuestions = entry.getValue();

            long correct = difficultyQuestions.stream()
                    .mapToLong(q -> q.isSubmittedAnswerCorrect() ? 1 : 0)
                    .sum();

            double accuracy = (double) correct / difficultyQuestions.size() * 100;

            double avgTime = difficultyQuestions.stream()
                    .mapToLong(q -> q.getAnswerTimeInMillis() != null ? q.getAnswerTimeInMillis() : 0L)
                    .filter(time -> time > 0)
                    .average()
                    .orElse(0.0) / 1000.0;

            prompt.append(String.format("%s: %.0f%% accuracy (%.1fs avg) ",
                    difficulty, accuracy, avgTime));
        }

        String bestDifficulty = questionsByDifficulty.entrySet().stream()
                .max((e1, e2) -> {
                    double acc1 = e1.getValue().stream().mapToLong(q -> q.isSubmittedAnswerCorrect() ? 1 : 0).sum()
                            / (double) e1.getValue().size();
                    double acc2 = e2.getValue().stream().mapToLong(q -> q.isSubmittedAnswerCorrect() ? 1 : 0).sum()
                            / (double) e2.getValue().size();
                    return Double.compare(acc1, acc2);
                })
                .map(Map.Entry::getKey)
                .orElse("MEDIUM");

        prompt.append(String.format("- Best performance on %s questions. ", bestDifficulty));
    }

    private String truncateText(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 3) + "...";
    }
}