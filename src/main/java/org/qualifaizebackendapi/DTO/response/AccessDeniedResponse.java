package org.qualifaizebackendapi.DTO.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response object for access denied errors")
public class AccessDeniedResponse {

    @Schema(description = "Error type", example = "Forbidden")
    private String error;

    @Schema(description = "Detailed error message", example = "You do not have permission to access this resource")
    private String message;

    @Schema(description = "Username of the user attempting access", example = "john_doe")
    private String user;

    @Schema(description = "Request path that was denied", example = "/api/v1/admin/users")
    private String path;

    @Schema(description = "HTTP status code", example = "403")
    private int status;
}