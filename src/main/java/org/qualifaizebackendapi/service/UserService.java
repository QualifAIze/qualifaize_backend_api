package org.qualifaizebackendapi.service;

import org.qualifaizebackendapi.DTO.request.UserLoginRequest;
import org.qualifaizebackendapi.DTO.request.UserRegisterRequest;
import org.qualifaizebackendapi.DTO.response.UserLoginResponse;
import org.qualifaizebackendapi.DTO.response.UserRegisterResponse;

public interface UserService {
    UserLoginResponse login(UserLoginRequest user);
    UserRegisterResponse register(UserRegisterRequest user);
}
