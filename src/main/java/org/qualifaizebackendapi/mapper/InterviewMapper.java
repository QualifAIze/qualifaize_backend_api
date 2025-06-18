package org.qualifaizebackendapi.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.qualifaizebackendapi.DTO.request.interview.CreateInterviewRequest;
import org.qualifaizebackendapi.model.Document;
import org.qualifaizebackendapi.model.Interview;
import org.qualifaizebackendapi.model.User;

@Mapper(componentModel = "spring")
public interface InterviewMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Interview toInterviewFromCreateInterviewRequest(CreateInterviewRequest request, Document document, User createdByUser, User assignedToUser);
}
