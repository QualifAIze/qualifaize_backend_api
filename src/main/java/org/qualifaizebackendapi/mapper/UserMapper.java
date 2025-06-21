package org.qualifaizebackendapi.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.qualifaizebackendapi.DTO.db_object.DocumentWithUserRow;
import org.qualifaizebackendapi.DTO.request.UserRegisterRequest;
import org.qualifaizebackendapi.DTO.response.UserAuthResponse;
import org.qualifaizebackendapi.DTO.response.user.UserDetailsOverviewResponse;
import org.qualifaizebackendapi.model.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserAuthResponse toUserAuthResponse(String token);

    @Mapping(target = "password", ignore = true)
    @Mapping(target = "roles", ignore = true)
    User toUser(UserRegisterRequest userRegisterRequest);

    @Named("toUserOverviewResponseFromDocumentWithUserRow")
    UserDetailsOverviewResponse toUserOverviewResponseFromDocumentWithUserRow(DocumentWithUserRow dbRow);
}
