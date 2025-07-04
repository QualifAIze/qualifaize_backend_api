package org.qualifaizebackendapi.mapper;

import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.qualifaizebackendapi.DTO.response.interview.question.GenerateQuestionDTO;
import org.qualifaizebackendapi.DTO.response.interview.question.QuestionDetailsResponse;
import org.qualifaizebackendapi.DTO.response.interview.question.QuestionToAsk;
import org.qualifaizebackendapi.DTO.response.interview.question.SubmitAnswerResponse;
import org.qualifaizebackendapi.model.Interview;
import org.qualifaizebackendapi.model.Question;

import java.util.List;

@Mapper(componentModel = "spring")
public interface QuestionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "difficulty", source = "generateQuestionDTO.difficulty")
    @Mapping(target = "interview", source = "interview")
    @Mapping(target = "questionOrder", ignore = true)
    @Mapping(target = "submittedAnswer", ignore = true)
    @Mapping(target = "answeredAt", ignore = true)
    Question toQuestion(GenerateQuestionDTO generateQuestionDTO, Interview interview);

    @Mapping(target = "questionId", source = "question.id")
    @Mapping(target = "title", source = "question.questionText")
    QuestionToAsk toQuestionToAsk(Question question);

    @Mapping(target = "questionId", source = "question.id")
    @Mapping(target = "correctAnswer", source = "question.correctOption")
    @Mapping(target = "correct", ignore = true)
    @Mapping(target = "currentProgress", ignore = true)
    SubmitAnswerResponse toSubmitAnswerResponse(Question question, String submittedAnswer);

    @Mapping(target = "isCorrect", expression = "java(getIsCorrect(question))")
    @Mapping(target = "answerTimeInMillis", expression = "java(getAnswerTimeInMillis(question))")
    @Named("toQuestionDetailsResponse")
    QuestionDetailsResponse toQuestionDetailsResponse(Question question);

    @IterableMapping(qualifiedByName = "toQuestionDetailsResponse")
    @Named("toQuestionDetailsResponses")
    List<QuestionDetailsResponse> toQuestionDetailsResponses(List<Question> questions);

    // Helper methods for QuestionMapper
    default Boolean getIsCorrect(Question question) {
        if (!question.isAnswered()) return null;
        return question.isSubmittedAnswerCorrect();
    }

    default Long getAnswerTimeInMillis(Question question) {
        return question.getAnswerTimeInMillis();
    }
}
