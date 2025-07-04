package org.qualifaizebackendapi.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.qualifaizebackendapi.DTO.QuestionDetailsDTO;
import org.qualifaizebackendapi.DTO.response.interview.question.GenerateQuestionDTO;
import org.qualifaizebackendapi.exception.ResourceNotFoundException;
import org.qualifaizebackendapi.mapper.QuestionMapper;
import org.qualifaizebackendapi.model.Interview;
import org.qualifaizebackendapi.model.Question;
import org.qualifaizebackendapi.repository.QuestionRepository;
import org.qualifaizebackendapi.service.AiQuestionGenerationService;
import org.qualifaizebackendapi.service.QuestionService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuestionServiceImpl implements QuestionService {

    private final QuestionRepository questionRepository;

    private final QuestionMapper questionMapper;

    private final AiQuestionGenerationService aiQuestionGenerationService;

    @Override
    public Question getNextQuestion(Interview interview) {
        GenerateQuestionDTO generatedQuestion = aiQuestionGenerationService
                .generateNextInterviewQuestion(interview, this.previousQuestionsAnalysisText(interview.getId()));

        Question questionEntity = questionMapper.toQuestion(generatedQuestion, interview);

        long currentQuestionCount = questionRepository.countByInterviewId(interview.getId());
        questionEntity.setQuestionOrder(Math.toIntExact(currentQuestionCount) + 1);

        Question savedQuestion = questionRepository.save(questionEntity);

        log.debug("Persisted question {} with order {} for interview: {}",
                savedQuestion.getId(), savedQuestion.getQuestionOrder(), interview.getId());

        return savedQuestion;
    }

    @Override
    public Question getQuestion(UUID questionId) {
        return questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Question with ID %s not found", questionId)
                ));
    }

    @Override
    public Question addUserSubmitAnswer(UUID questionId, String submitAnswer) {
        Question question = this.getQuestion(questionId);
        question.submitAnswer(submitAnswer);
        return questionRepository.save(question);
    }


    @Override
    public String previousQuestionsAnalysisText(UUID interviewId) {
        List<QuestionDetailsDTO> previousQuestions = questionRepository.findQuestionsDetailsByInterviewId(interviewId);

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
            QuestionDetailsDTO question = previousQuestions.get(i);

            prompt.append(String.format("%d. ", i + 1));

            String questionText = question.getQuestionText();
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

    private TimingAnalysis analyzeTimingPatterns(List<QuestionDetailsDTO> questions) {
        List<Long> answerTimes = questions.stream()
                .map(QuestionDetailsDTO::getAnswerTimeInMillis)
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

    private void addPerformanceTrend(StringBuilder prompt, List<QuestionDetailsDTO> questions) {
        int recentCount = Math.min(3, questions.size());
        List<QuestionDetailsDTO> recentQuestions = questions.subList(
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

    private void addTimingPatternAnalysis(StringBuilder prompt, List<QuestionDetailsDTO> questions, TimingAnalysis overall) {
        List<QuestionDetailsDTO> correctAnswers = questions.stream()
                .filter(QuestionDetailsDTO::isSubmittedAnswerCorrect)
                .toList();

        List<QuestionDetailsDTO> incorrectAnswers = questions.stream()
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

    private void addAnswerPatternAnalysis(StringBuilder prompt, List<QuestionDetailsDTO> questions) {
        Map<String, Long> answerCounts = questions.stream()
                .collect(Collectors.groupingBy(
                        QuestionDetailsDTO::getSubmittedAnswer,
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

    private void addDifficultyPatternAnalysis(StringBuilder prompt, List<QuestionDetailsDTO> questions) {
        Map<String, List<QuestionDetailsDTO>> questionsByDifficulty = questions.stream()
                .collect(Collectors.groupingBy(q -> q.getDifficulty().toString()));

        for (Map.Entry<String, List<QuestionDetailsDTO>> entry : questionsByDifficulty.entrySet()) {
            String difficulty = entry.getKey();
            List<QuestionDetailsDTO> difficultyQuestions = entry.getValue();

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
}
