package org.qualifaizebackendapi.service;

import org.qualifaizebackendapi.DTO.request.user.UpdateUserDetailsRequest;
import org.qualifaizebackendapi.DTO.request.user.UserLoginRequest;
import org.qualifaizebackendapi.DTO.request.user.UserRegisterRequest;
import org.qualifaizebackendapi.DTO.response.UserAuthResponse;
import org.qualifaizebackendapi.DTO.response.user.UserDetailsOverviewResponse;
import org.qualifaizebackendapi.DTO.response.user.UserDetailsResponse;
import org.qualifaizebackendapi.model.User;

import java.util.List;
import java.util.UUID;

public interface UserService {
    UserAuthResponse login(UserLoginRequest user);
    UserAuthResponse register(UserRegisterRequest user);
    void deleteUser(UUID userId);
    List<UserDetailsOverviewResponse> getAllUsers();
    User fetchUserOrThrow(UUID userId);
    UserDetailsResponse updateUserDetails(UUID userId, UpdateUserDetailsRequest request);
    UserDetailsResponse getCurrentUserDetails();
}
