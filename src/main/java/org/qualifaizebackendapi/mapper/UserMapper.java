package org.qualifaizebackendapi.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.qualifaizebackendapi.DTO.response.RegisteredUserResponse;
import org.qualifaizebackendapi.model.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "roles", source = "user.roles")
    RegisteredUserResponse userToRegisteredUserResponse(User user, String token);
}
