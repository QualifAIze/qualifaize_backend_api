package org.qualifaizebackendapi.mapper;


import org.mapstruct.*;
import org.qualifaizebackendapi.DTO.db_object.DocumentWithUserRow;
import org.qualifaizebackendapi.DTO.request.user.UpdateUserDetailsRequest;
import org.qualifaizebackendapi.DTO.request.user.UserRegisterRequest;
import org.qualifaizebackendapi.DTO.response.UserAuthResponse;
import org.qualifaizebackendapi.DTO.response.user.UserDetailsOverviewResponse;
import org.qualifaizebackendapi.DTO.response.user.UserDetailsResponse;
import org.qualifaizebackendapi.model.User;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserAuthResponse toUserAuthResponse(String token);

    @Mapping(target = "password", ignore = true)
    @Mapping(target = "roles", ignore = true)
    User toUser(UserRegisterRequest userRegisterRequest);

    @Named("toUserOverviewResponseFromDocumentWithUserRow")
    UserDetailsOverviewResponse toUserOverviewResponseFromDocumentWithUserRow(DocumentWithUserRow dbRow);

    @Mapping(target = "userId", source = "id")
    @Named("toUserDetailsOverviewResponse")
    UserDetailsOverviewResponse toUserDetailsOverviewResponse(User user);

    @IterableMapping(qualifiedByName = "toUserDetailsOverviewResponse")
    List<UserDetailsOverviewResponse> toUserDetailsOverviewResponseList(List<User> users);

    @Mapping(target = "userId", source = "id")
    @Mapping(target = "memberSince", source = "createdAt")
    UserDetailsResponse toUserDetailsResponse(User user);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    void updateUserFromRequest(UpdateUserDetailsRequest request, @MappingTarget User user);
}