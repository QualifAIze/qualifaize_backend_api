package org.qualifaizebackendapi.DTO.request;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for user registration")
public class UserRegisterRequest {

    @NotBlank(message = "Username is required")
    @Schema(description = "Desired username", example = "john_doe", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    @NotBlank(message = "Email is required")
    @Schema(description = "Email of the user", example = "john_doe@gmail.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotBlank(message = "Password is required")
    @Schema(description = "Password for the account", example = "password123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @NotBlank(message = "First name is required")
    @Schema(description = "First name of the user", example = "John", requiredMode = Schema.RequiredMode.REQUIRED)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Schema(description = "Last name of the user", example = "Doe", requiredMode = Schema.RequiredMode.REQUIRED)
    private String lastName;

    @NotNull(message = "Birthdate is required")
    @Schema(
            description = "Birthdate of the user (ISO 8601, e.g., 1990-01-01T00:00:00Z)",
            example = "1990-01-01T00:00:00Z",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private OffsetDateTime birthDate;

    @NotNull(message = "Roles are required")
    @Schema(description = "Array of roles to assign to the user", example = "[\"GUEST\", \"USER\"]", requiredMode = Schema.RequiredMode.REQUIRED)
    private String[] roles;
}
