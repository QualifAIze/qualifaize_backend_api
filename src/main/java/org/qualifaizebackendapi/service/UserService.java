package org.qualifaizebackendapi.service;

import org.qualifaizebackendapi.DTO.request.UserLoginRequest;
import org.qualifaizebackendapi.DTO.request.UserRegisterRequest;
import org.qualifaizebackendapi.DTO.response.UserAuthResponse;
import org.qualifaizebackendapi.DTO.response.user.UserDetailsOverviewResponse;
import org.qualifaizebackendapi.model.User;

import java.util.List;
import java.util.UUID;

public interface UserService {
    UserAuthResponse login(UserLoginRequest user);
    UserAuthResponse register(UserRegisterRequest user);
    void deleteUser(UUID userId);
    List<UserDetailsOverviewResponse> getAllUsers();
    User fetchUserOrThrow(UUID userId);
}
