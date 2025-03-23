package org.qualifaizebackendapi.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.qualifaizebackendapi.DTO.response.AccessDeniedResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        // Get authenticated username
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = (authentication != null && authentication.getName() != null) ? authentication.getName() : "Anonymous";

        // Create JSON response
        AccessDeniedResponse errorResponse = new AccessDeniedResponse(
                "Forbidden",
                "You do not have permission to access this resource",
                username,
                request.getRequestURI(),
                HttpStatus.FORBIDDEN.value()
        );

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}