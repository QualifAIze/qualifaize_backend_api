package org.qualifaizebackendapi.DTO.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for user login")
public class UserLoginRequest {

    @NotBlank(message = "Username is required")
    @Schema(description = "Username for login", example = "john_doe", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    @NotBlank(message = "Password is required")
    @Schema(description = "Password for login", example = "password123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;
}