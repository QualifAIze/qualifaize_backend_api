package org.qualifaizebackendapi.mapper;

import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.qualifaizebackendapi.DTO.request.interview.CreateInterviewRequest;
import org.qualifaizebackendapi.DTO.response.interview.AssignedInterviewResponse;
import org.qualifaizebackendapi.model.Document;
import org.qualifaizebackendapi.model.Interview;
import org.qualifaizebackendapi.model.User;

import java.util.List;

@Mapper(componentModel = "spring")
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
}
