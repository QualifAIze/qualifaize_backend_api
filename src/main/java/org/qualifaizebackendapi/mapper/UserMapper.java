package org.qualifaizebackendapi.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.qualifaizebackendapi.DTO.db_object.DocumentWithUserRow;
import org.qualifaizebackendapi.DTO.response.UserRegisterResponse;
import org.qualifaizebackendapi.DTO.response.user.UserDetailsOverviewResponse;
import org.qualifaizebackendapi.model.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "roles", source = "user.roles")
    UserRegisterResponse userToRegisteredUserResponse(User user, String token);

    @Named("toUserOverviewResponseFromDocumentWithUserRow")
    UserDetailsOverviewResponse toUserOverviewResponseFromDocumentWithUserRow(DocumentWithUserRow dbRow);
}
