package org.qualifaizebackendapi.DTO.response;

import org.qualifaizebackendapi.model.enums.Role;

import java.util.Set;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response object after successful registration")
public class UserRegisterResponse {

    @Schema(description = "Username of the registered user", example = "john_doe")
    private String username;

    @Schema(description = "JWT authentication token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;

    @Schema(description = "Set of roles assigned to the user")
    private Set<Role> roles;
}