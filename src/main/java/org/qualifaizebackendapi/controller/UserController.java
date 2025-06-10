package org.qualifaizebackendapi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.qualifaizebackendapi.DTO.request.UserLoginRequest;
import org.qualifaizebackendapi.DTO.request.UserRegisterRequest;
import org.qualifaizebackendapi.DTO.response.AccessDeniedResponse;
import org.qualifaizebackendapi.DTO.response.UserRegisterResponse;
import org.qualifaizebackendapi.DTO.response.UserLoginResponse;
import org.qualifaizebackendapi.service.impl.UserServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User Operations", description = "Operations related to user registration and login")
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@RestController
public class UserController {

    private final UserServiceImpl userServiceImpl;

    @Operation(
            summary = "Register a new user account",
            description = "Creates a new user in the system using the provided registration details and returns the newly registered user’s information.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved path information",
                            content = @Content(schema = @Schema(implementation = UserRegisterResponse.class))
                    )
            }
    )
    @PostMapping("/register")
    public ResponseEntity<UserRegisterResponse> register(@RequestBody UserRegisterRequest userRegisterRequestDTO){
        return ResponseEntity.ok(userServiceImpl.register(userRegisterRequestDTO));
    }

    @Operation(
            summary = "Authenticate an existing user",
            description = "Verifies the provided username and password, and returns authentication details (e.g., token) if successful.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved path information",
                            content = @Content(schema = @Schema(implementation = UserLoginResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Invalid username or password",
                            content = @Content(schema = @Schema(implementation = AccessDeniedResponse.class))
                    )
            }
    )
    @PostMapping("/login")
    public ResponseEntity<UserLoginResponse> login(@RequestBody UserLoginRequest userLoginRequest){
        return ResponseEntity.ok(userServiceImpl.login(userLoginRequest));
    }

}
