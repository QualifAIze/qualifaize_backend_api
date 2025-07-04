package org.qualifaizebackendapi.service;

import org.qualifaizebackendapi.DTO.response.interview.question.QuestionSectionResponse;
import org.qualifaizebackendapi.model.Interview;

public interface AiSectionSelectionService {
    QuestionSectionResponse selectSectionForNextQuestion(Interview interview, String previousQuestionsAnalysisText);
}
