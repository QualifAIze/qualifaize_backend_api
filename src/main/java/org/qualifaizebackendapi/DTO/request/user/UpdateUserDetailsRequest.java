package org.qualifaizebackendapi.DTO.request.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for updating user details")
public class UpdateUserDetailsRequest {

    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Schema(description = "Username (optional)", example = "john_doe_updated")
    private String username;

    @Email(message = "Email should be valid")
    @Schema(description = "Email address (optional)", example = "john.doe.updated@gmail.com")
    private String email;

    @Size(min = 1, max = 100, message = "First name must be between 1 and 100 characters")
    @Schema(description = "First name (optional)", example = "John")
    private String firstName;

    @Size(min = 1, max = 100, message = "Last name must be between 1 and 100 characters")
    @Schema(description = "Last name (optional)", example = "Doe")
    private String lastName;

    @Schema(description = "Birth date (optional)", example = "1990-05-15T00:00:00Z")
    private OffsetDateTime birthDate;
}