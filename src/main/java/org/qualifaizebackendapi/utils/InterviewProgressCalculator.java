package org.qualifaizebackendapi.utils;

import lombok.extern.slf4j.Slf4j;
import org.qualifaizebackendapi.DTO.QuestionDetailsDTO;
import org.qualifaizebackendapi.model.enums.Difficulty;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public final class InterviewProgressCalculator {

    private InterviewProgressCalculator() {
    }

    /**
     * Calculates dynamic interview progress based on performance, speed, difficulty, and confidence
     */
    public static int calculateProgress(List<QuestionDetailsDTO> answeredQuestions) {
        if (answeredQuestions == null || answeredQuestions.isEmpty()) {
            return 0;
        }

        int baseProgress = Math.min(answeredQuestions.size() * 10, 70);
        double performanceFactor = calculatePerformanceFactor(answeredQuestions);
        double speedFactor = calculateSpeedFactor(answeredQuestions);
        double difficultyFactor = calculateDifficultyFactor(answeredQuestions);
        double confidenceFactor = calculateConfidenceFactor(answeredQuestions);

        double finalProgress = baseProgress * performanceFactor * speedFactor * difficultyFactor * confidenceFactor;

        int progressPercentage = (int) Math.round(Math.min(99.0, finalProgress));

        // Force 100% completion for edge cases
        if (answeredQuestions.size() >= 25 ||
                (answeredQuestions.size() >= 10 && finalProgress >= 95)) {
            progressPercentage = 100;
        }

        log.debug("Interview progress: base={}%, perf={}, speed={}, diff={}, conf={}, result={}%",
                baseProgress, String.format("%.2f", performanceFactor), String.format("%.2f", speedFactor),
                String.format("%.2f", difficultyFactor), String.format("%.2f", confidenceFactor), progressPercentage);

        return answeredQuestions.size() * 25;
    }

    private static double calculatePerformanceFactor(List<QuestionDetailsDTO> questions) {
        if (questions.size() < 3) {
            return 1.0;
        }

        double totalWeight = 0;
        double weightedCorrect = 0;

        for (QuestionDetailsDTO question : questions) {
            double weight = getDifficultyWeight(question.getDifficulty());
            totalWeight += weight;

            if (question.isSubmittedAnswerCorrect()) {
                weightedCorrect += weight;
            }
        }

        double weightedAccuracy = weightedCorrect / totalWeight;

        if (weightedAccuracy >= 0.9) {
            return 1.5;
        } else if (weightedAccuracy >= 0.8) {
            return 1.2 + (weightedAccuracy - 0.8) * 3;
        } else if (weightedAccuracy >= 0.6) {
            return 1.0 + (weightedAccuracy - 0.6) * 1;
        } else if (weightedAccuracy >= 0.4) {
            return 0.8 + (weightedAccuracy - 0.4) * 1;
        } else {
            return 0.5 + weightedAccuracy * 0.75;
        }
    }

    private static double calculateSpeedFactor(List<QuestionDetailsDTO> questions) {
        List<Long> validTimes = questions.stream()
                .map(QuestionDetailsDTO::getAnswerTimeInMillis)
                .filter(time -> time != null && time > 0)
                .toList();

        if (validTimes.isEmpty()) {
            return 1.0;
        }

        double averageSeconds = validTimes.stream()
                .mapToDouble(time -> time / 1000.0)
                .average()
                .orElse(60.0);

        double fastAccuracy = questions.stream()
                .filter(q -> q.getAnswerTimeInMillis() != null && q.getAnswerTimeInMillis() < 20000)
                .mapToDouble(q -> q.isSubmittedAnswerCorrect() ? 1.0 : 0.0)
                .average()
                .orElse(0.5);

        if (averageSeconds < 15) {
            return fastAccuracy > 0.7 ? 1.3 : 0.8;
        } else if (averageSeconds < 30) {
            return 1.1;
        } else if (averageSeconds < 60) {
            return 1.0;
        } else if (averageSeconds < 120) {
            return 0.95;
        } else {
            return 0.8;
        }
    }

    private static double calculateDifficultyFactor(List<QuestionDetailsDTO> questions) {
        Map<Difficulty, List<QuestionDetailsDTO>> byDifficulty = questions.stream()
                .collect(Collectors.groupingBy(QuestionDetailsDTO::getDifficulty));

        boolean hasEasy = byDifficulty.containsKey(Difficulty.EASY);
        boolean hasMedium = byDifficulty.containsKey(Difficulty.MEDIUM);
        boolean hasHard = byDifficulty.containsKey(Difficulty.HARD);

        double easyAccuracy = calculateDifficultyAccuracy(byDifficulty.get(Difficulty.EASY));
        double mediumAccuracy = calculateDifficultyAccuracy(byDifficulty.get(Difficulty.MEDIUM));
        double hardAccuracy = calculateDifficultyAccuracy(byDifficulty.get(Difficulty.HARD));

        if (hasHard && hardAccuracy > 0.6) {
            return 1.4;
        } else if (hasMedium && mediumAccuracy > 0.7) {
            return 1.2;
        } else if (hasEasy && easyAccuracy > 0.8) {
            return 1.0;
        } else {
            return 0.9;
        }
    }

    private static double calculateConfidenceFactor(List<QuestionDetailsDTO> questions) {
        if (questions.size() < 3) {
            return 1.0;
        }

        List<QuestionDetailsDTO> recent = questions.subList(
                Math.max(0, questions.size() - 3), questions.size());

        long recentCorrect = recent.stream()
                .mapToLong(q -> q.isSubmittedAnswerCorrect() ? 1 : 0)
                .sum();

        if (questions.size() >= 6) {
            List<QuestionDetailsDTO> earlier = questions.subList(
                    Math.max(0, questions.size() - 6), questions.size() - 3);

            long earlierCorrect = earlier.stream()
                    .mapToLong(q -> q.isSubmittedAnswerCorrect() ? 1 : 0)
                    .sum();

            double recentAccuracy = recentCorrect / 3.0;
            double earlierAccuracy = earlierCorrect / 3.0;

            if (recentAccuracy > earlierAccuracy + 0.2) {
                return 1.2;
            } else if (recentAccuracy < earlierAccuracy - 0.2) {
                return 0.7;
            }
        }

        if (recentCorrect == 3) {
            return 1.15;
        } else if (recentCorrect == 2) {
            return 1.0;
        } else if (recentCorrect == 1) {
            return 0.9;
        } else {
            return 0.8;
        }
    }

    private static double getDifficultyWeight(Difficulty difficulty) {
        return switch (difficulty) {
            case EASY -> 1.0;
            case MEDIUM -> 1.5;
            case HARD -> 2.0;
        };
    }

    private static double calculateDifficultyAccuracy(List<QuestionDetailsDTO> questions) {
        if (questions == null || questions.isEmpty()) {
            return 0.0;
        }

        long correct = questions.stream()
                .mapToLong(q -> q.isSubmittedAnswerCorrect() ? 1 : 0)
                .sum();

        return (double) correct / questions.size();
    }
}