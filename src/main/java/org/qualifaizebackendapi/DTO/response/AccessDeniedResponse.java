package org.qualifaizebackendapi.DTO.response;

public record AccessDeniedResponse(
        String error,
        String message,
        String user,
        String path,
        int status
) {}