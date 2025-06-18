package org.qualifaizebackendapi.DTO.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for user registration")
public class UserRegisterRequest {

    @NotBlank(message = "Username is required")
    @Schema(description = "Desired username", example = "john_doe", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    @NotBlank(message = "Password is required")
    @Schema(description = "Password for the account", example = "password123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @NotNull(message = "Roles are required")
    @Schema(description = "Array of roles to assign to the user", example = "[\"USER\", \"ADMIN\"]", requiredMode = Schema.RequiredMode.REQUIRED)
    private String[] roles;
}
