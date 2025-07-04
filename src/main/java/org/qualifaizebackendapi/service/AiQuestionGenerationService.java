package org.qualifaizebackendapi.service;

import org.qualifaizebackendapi.DTO.response.interview.question.GenerateQuestionDTO;
import org.qualifaizebackendapi.model.Interview;

public interface AiQuestionGenerationService {
    GenerateQuestionDTO generateNextInterviewQuestion(Interview interviewInterview, String previousQuestionAnalysisText);
}