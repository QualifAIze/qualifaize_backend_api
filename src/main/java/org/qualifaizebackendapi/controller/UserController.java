package org.qualifaizebackendapi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.qualifaizebackendapi.DTO.request.user.UpdateUserDetailsRequest;
import org.qualifaizebackendapi.DTO.request.user.UserLoginRequest;
import org.qualifaizebackendapi.DTO.request.user.UserRegisterRequest;
import org.qualifaizebackendapi.DTO.response.AccessDeniedResponse;
import org.qualifaizebackendapi.DTO.response.UserAuthResponse;
import org.qualifaizebackendapi.DTO.response.user.UserDetailsOverviewResponse;
import org.qualifaizebackendapi.DTO.response.user.UserDetailsResponse;
import org.qualifaizebackendapi.exception.ErrorResponse;
import org.qualifaizebackendapi.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
                            content = @Content(schema = @Schema(implementation = UserAuthResponse.class))
                    )
            }
    )
    @PostMapping("/auth/register")
    public ResponseEntity<UserAuthResponse> register(@RequestBody UserRegisterRequest userRegisterRequestDTO) {
        return ResponseEntity.ok(userService.register(userRegisterRequestDTO));
    }

    @Operation(
            summary = "Authenticate an existing user",
            description = "Verifies the provided username and password, and returns authentication details (e.g., token) if successful.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved path information",
                            content = @Content(schema = @Schema(implementation = UserAuthResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Invalid username or password",
                            content = @Content(schema = @Schema(implementation = AccessDeniedResponse.class))
                    )
            }
    )
    @PostMapping("/auth/login")
    public ResponseEntity<UserAuthResponse> login(@RequestBody UserLoginRequest userLoginRequest) {
        return ResponseEntity.ok(userService.login(userLoginRequest));
    }

    @Operation(
            summary = "Get all users",
            description = "Retrieves a list of all active users in the system with basic information including userId, username, firstName, and lastName.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Users retrieved successfully",
                            content = @Content(
                                    array = @ArraySchema(schema = @Schema(implementation = UserDetailsOverviewResponse.class))
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Insufficient permissions to access user list",
                            content = @Content(schema = @Schema(implementation = AccessDeniedResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
                    )
            }
    )
    @GetMapping
    public ResponseEntity<List<UserDetailsOverviewResponse>> getAllUsers() {
        List<UserDetailsOverviewResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @Operation(
            summary = "Get current user details",
            description = "Retrieves the detailed information of the currently authenticated user including personal information, roles, and account status."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Current user details retrieved successfully",
                    content = @Content(schema = @Schema(implementation = UserDetailsResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "User not authenticated",
                    content = @Content(schema = @Schema(implementation = AccessDeniedResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Current user not found (possibly deleted)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @GetMapping("/me")
    public ResponseEntity<UserDetailsResponse> getCurrentUserDetails() {
        UserDetailsResponse response = userService.getCurrentUserDetails();
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Update user details",
            description = "Updates the details of an existing user. Only provided fields will be updated, null fields are ignored. Username and email uniqueness is validated."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "User details updated successfully",
                    content = @Content(schema = @Schema(implementation = UserDetailsResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request data or validation errors",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Username or email already exists",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Insufficient permissions to update user",
                    content = @Content(schema = @Schema(implementation = AccessDeniedResponse.class))
            )
    })
    @PutMapping("/{userId}")
    public ResponseEntity<UserDetailsResponse> updateUserDetails(
            @Parameter(description = "ID of the user to update", required = true)
            @PathVariable UUID userId,

            @Parameter(description = "User details to update (only non-null fields will be updated)", required = true)
            @Valid @RequestBody UpdateUserDetailsRequest request
    ) {
        UserDetailsResponse response = userService.updateUserDetails(userId, request);
        return ResponseEntity.ok(response);
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
    public ResponseEntity<Void> deleteUser(@PathVariable UUID userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}
