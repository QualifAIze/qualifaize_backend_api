package org.qualifaizebackendapi.mapper;

import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.qualifaizebackendapi.DTO.request.interview.CreateInterviewRequest;
import org.qualifaizebackendapi.DTO.response.interview.AssignedInterviewResponse;
import org.qualifaizebackendapi.DTO.response.interview.InterviewDetailsResponse;
import org.qualifaizebackendapi.model.Document;
import org.qualifaizebackendapi.model.Interview;
import org.qualifaizebackendapi.model.User;

import java.util.List;

@Mapper(componentModel = "spring", uses = {QuestionMapper.class, UserMapper.class})
public interface InterviewMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Interview toInterviewFromCreateInterviewRequest(CreateInterviewRequest request, Document document, User createdByUser, User assignedToUser);

    @Mapping(target = "interviewId", source = "id")
    @Mapping(target = "createdBy", expression = "java(getFullName(interview.getCreatedByUser()))")
    AssignedInterviewResponse toAssignedInterviewResponse(Interview interview);

    @IterableMapping(elementTargetType = AssignedInterviewResponse.class)
    List<AssignedInterviewResponse> toAssignedInterviewResponses(List<Interview> interviews);

    default String getFullName(User user) {
        if (user == null) return "Unknown";

        String firstName = user.getFirstName() != null ? user.getFirstName() : "";
        String lastName = user.getLastName() != null ? user.getLastName() : "";

        return (firstName + " " + lastName).trim();
    }

    @Mapping(target = "documentTitle", source = "interview.document.secondaryFileName")
    @Mapping(target = "createdBy", source = "createdByUser", qualifiedByName = "toUserDetailsOverviewResponse")
    @Mapping(target = "assignedTo", source = "assignedToUser", qualifiedByName = "toUserDetailsOverviewResponse")
    @Mapping(target = "questions", source = "questions", qualifiedByName = "toQuestionDetailsResponses")
    @Mapping(target = "totalQuestions", expression = "java(getTotalQuestions(interview))")
    @Mapping(target = "durationInSeconds", expression = "java(getDurationInSeconds(interview))")
    InterviewDetailsResponse toInterviewDetailsResponse(Interview interview);

    @IterableMapping(elementTargetType = InterviewDetailsResponse.class)
    List<InterviewDetailsResponse> toInterviewDetailsResponses(List<Interview> interviews);

    default Integer getTotalQuestions(Interview interview) {
        return interview.getQuestions() != null ? interview.getQuestions().size() : 0;
    }

    default Long getDurationInSeconds(Interview interview) {
        return interview != null ? interview.getDurationInSeconds() : null;
    }
}
