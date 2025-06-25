package org.qualifaizebackendapi.service;

import org.qualifaizebackendapi.DTO.response.interview.question.GenerateQuestionDTO;
import org.qualifaizebackendapi.DTO.response.interview.question.QuestionSectionResponse;
import org.qualifaizebackendapi.model.Interview;

public interface AiInterviewGenerationService {

    QuestionSectionResponse selectDocumentSectionForQuestion(Interview interview);

    GenerateQuestionDTO generateNextInterviewQuestion(Interview interview);
}