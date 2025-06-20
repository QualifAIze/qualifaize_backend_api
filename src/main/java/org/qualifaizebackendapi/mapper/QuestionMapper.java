package org.qualifaizebackendapi.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.qualifaizebackendapi.DTO.response.interview.question.GenerateQuestionDTO;
import org.qualifaizebackendapi.DTO.response.interview.question.QuestionToAsk;
import org.qualifaizebackendapi.DTO.response.interview.question.SubmitAnswerResponse;
import org.qualifaizebackendapi.model.Interview;
import org.qualifaizebackendapi.model.Question;

@Mapper(componentModel = "spring")
public interface QuestionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "difficulty", source = "generateQuestionDTO.difficulty")
    @Mapping(target = "interview", source = "interview")
    Question toQuestion(GenerateQuestionDTO generateQuestionDTO, Interview interview);

    @Mapping(target = "questionId", source = "question.id")
    @Mapping(target = "title", source = "question.questionText")
    QuestionToAsk toQuestionToAsk(Question question);

    @Mapping(target = "questionId", source = "question.id")
    @Mapping(target = "correctAnswer", source = "question.correctOption")
    SubmitAnswerResponse toSubmitAnswerResponse(Question question, String submittedAnswer);
}
