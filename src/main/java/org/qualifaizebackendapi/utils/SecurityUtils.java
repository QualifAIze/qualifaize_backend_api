package org.qualifaizebackendapi.utils;

import org.qualifaizebackendapi.model.User;
import org.qualifaizebackendapi.security.QualifAIzeUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

public class SecurityUtils {

    private SecurityUtils() {}

    private static Authentication getAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user found");
        }

        return authentication;
    }

    private static QualifAIzeUserDetails getUserDetails() {
        Object principal = getAuthentication().getPrincipal();

        if (principal instanceof QualifAIzeUserDetails) {
            return (QualifAIzeUserDetails) principal;
        } else {
            throw new IllegalStateException("Principal is not of expected type");
        }
    }

    public static User getCurrentUser() {
        return getUserDetails().getUser();
    }

    public static UUID getCurrentUserId() {
        return getUserDetails().getId();
    }
}
