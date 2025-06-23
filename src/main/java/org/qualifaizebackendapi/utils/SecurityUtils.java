package org.qualifaizebackendapi.utils;

import org.qualifaizebackendapi.model.User;
import org.qualifaizebackendapi.model.enums.Role;
import org.qualifaizebackendapi.security.QualifAIzeUserDetails;
import org.springframework.security.access.AccessDeniedException;
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

    /**
     * Check if current user has the specified role
     */
    public static boolean hasRole(Role role) {
        User currentUser = getCurrentUser();
        return currentUser.getRoles().contains(role);
    }

    /**
     * Check if current user has any of the specified roles
     */
    public static boolean hasAnyRole(Role... roles) {
        User currentUser = getCurrentUser();
        for (Role role : roles) {
            if (currentUser.getRoles().contains(role)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if current user has admin role
     */
    public static boolean isAdmin() {
        return hasRole(Role.ADMIN);
    }

    /**
     * Check if current user has user role (or higher)
     */
    public static boolean isUserOrAdmin() {
        return hasAnyRole(Role.USER, Role.ADMIN);
    }

    /**
     * Check if current user is the owner of the resource or has admin role
     */
    public static boolean isOwnerOrAdmin(UUID resourceOwnerId) {
        if (resourceOwnerId == null) {
            return false;
        }

        UUID currentUserId = getCurrentUserId();
        return currentUserId.equals(resourceOwnerId) || isAdmin();
    }

    /**
     * Check if current user can access/modify the specified user
     * Users can modify their own data, Admins can modify any user
     *
     * @param targetUserId The ID of the user being accessed
     * @throws AccessDeniedException if access is denied
     */
    public static void checkUserAccess(UUID targetUserId) {
        UUID currentUserId = getCurrentUserId();

        // User can access their own data
        if (currentUserId.equals(targetUserId)) {
            return;
        }

        // Admin can access any user's data
        if (isAdmin()) {
            return;
        }

        throw new AccessDeniedException("You can only access your own user details");
    }

    /**
     * Check if current user can perform admin operations
     *
     * @throws AccessDeniedException if user is not admin
     */
    public static void requireAdmin() {
        if (!isAdmin()) {
            throw new AccessDeniedException("Admin access required");
        }
    }

    /**
     * Check if current user can perform admin operations (with custom message)
     *
     * @param operation Description of the operation requiring admin access
     * @throws AccessDeniedException if user is not admin
     */
    public static void requireAdmin(String operation) {
        if (!isAdmin()) {
            throw new AccessDeniedException("Admin access required for: " + operation);
        }
    }

    /**
     * Check if current user can create/manage interviews
     *
     * @throws AccessDeniedException if user doesn't have required role
     */
    public static void requireInterviewManagementAccess() {
        if (!hasAnyRole(Role.USER, Role.ADMIN)) {
            throw new AccessDeniedException("USER or ADMIN role required for interview management");
        }
    }

    /**
     * Check if current user can upload/manage documents
     *
     * @throws AccessDeniedException if user doesn't have required role
     */
    public static void requireDocumentManagementAccess() {
        if (!hasAnyRole(Role.USER, Role.ADMIN)) {
            throw new AccessDeniedException("USER or ADMIN role required for document management");
        }
    }

    /**
     * Check if current user is the owner of the resource or admin
     *
     * @param resourceOwnerId The ID of the resource owner
     * @throws AccessDeniedException if user is not owner and not admin
     */
    public static void requireOwnershipOrAdmin(UUID resourceOwnerId) {
        if (!isOwnerOrAdmin(resourceOwnerId)) {
            throw new AccessDeniedException("You can only access your own resources");
        }
    }

    /**
     * Check if current user is the owner of the resource or admin (with custom message)
     *
     * @param resourceOwnerId The ID of the resource owner
     * @param resourceType Description of the resource type
     * @throws AccessDeniedException if user is not owner and not admin
     */
    public static void requireOwnershipOrAdmin(UUID resourceOwnerId, String resourceType) {
        if (!isOwnerOrAdmin(resourceOwnerId)) {
            throw new AccessDeniedException("You can only access your own " + resourceType);
        }
    }

    /**
     * Check if current user has the specified role
     *
     * @param role The required role
     * @throws AccessDeniedException if user doesn't have the role
     */
    public static void requireRole(Role role) {
        if (!hasRole(role)) {
            throw new AccessDeniedException(role.name() + " role required");
        }
    }

    /**
     * Check if current user has any of the specified roles
     *
     * @param roles The required roles (user needs at least one)
     * @throws AccessDeniedException if user doesn't have any of the roles
     */
    public static void requireAnyRole(Role... roles) {
        if (!hasAnyRole(roles)) {
            StringBuilder roleNames = new StringBuilder();
            for (int i = 0; i < roles.length; i++) {
                if (i > 0) {
                    roleNames.append(i == roles.length - 1 ? " or " : ", ");
                }
                roleNames.append(roles[i].name());
            }
            throw new AccessDeniedException(roleNames + " role required");
        }
    }

    /**
     * Check if user can modify the entity (either owner or admin)
     * Useful for documents, interviews, etc.
     *
     * @param entityOwnerId The ID of the entity owner
     * @return true if user can modify, false otherwise
     */
    public static boolean canModifyEntity(UUID entityOwnerId) {
        return isOwnerOrAdmin(entityOwnerId);
    }

    /**
     * Get current user ID safely (returns null if no authenticated user)
     *
     * @return Current user ID or null
     */
    public static UUID getCurrentUserIdSafe() {
        try {
            return getCurrentUserId();
        } catch (IllegalStateException e) {
            return null;
        }
    }

    /**
     * Get current user safely (returns null if no authenticated user)
     *
     * @return Current user or null
     */
    public static User getCurrentUserSafe() {
        try {
            return getCurrentUser();
        } catch (IllegalStateException e) {
            return null;
        }
    }

    /**
     * Check if there is an authenticated user
     *
     * @return true if user is authenticated, false otherwise
     */
    public static boolean isAuthenticated() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            return authentication != null && authentication.isAuthenticated() &&
                    !"anonymousUser".equals(authentication.getPrincipal());
        } catch (Exception e) {
            return false;
        }
    }
}