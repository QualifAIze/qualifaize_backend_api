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
import org.qualifaizebackendapi.service.UserService;
import org.qualifaizebackendapi.service.impl.UserServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "User Operations", description = "Operations related to user registration and login")
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
@RestController
public class UserController {

    private final UserService userService;

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
    @PostMapping("/auth/register")
    public ResponseEntity<UserRegisterResponse> register(@RequestBody UserRegisterRequest userRegisterRequestDTO){
        return ResponseEntity.ok(userService.register(userRegisterRequestDTO));
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
    @PostMapping("/auth/login")
    public ResponseEntity<UserLoginResponse> login(@RequestBody UserLoginRequest userLoginRequest){
        return ResponseEntity.ok(userService.login(userLoginRequest));
    }

    @Operation(
            summary = "Delete user account",
            description = "Performs a soft delete on the specified user account, marking it as deleted while preserving the data in the system.",
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "User successfully deleted"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "User not found",
                            content = @Content(schema = @Schema(implementation = AccessDeniedResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Unauthorized to delete this user",
                            content = @Content(schema = @Schema(implementation = AccessDeniedResponse.class))
                    )
            }
    )
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID userId){
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

}
