package org.qualifaizebackendapi.service;

import org.qualifaizebackendapi.model.Interview;
import org.qualifaizebackendapi.model.Question;

import java.util.UUID;

public interface QuestionService {
    Question getNextQuestion(Interview interview);
    Question getQuestion(UUID questionId);
    Question addUserSubmitAnswer(UUID questionId, String submitAnswer);
    String previousQuestionsAnalysisText(UUID interviewId);
}
