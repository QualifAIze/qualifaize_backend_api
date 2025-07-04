package org.qualifaizebackendapi.service;

import org.qualifaizebackendapi.DTO.request.user.UpdateUserDetailsRequest;
import org.qualifaizebackendapi.DTO.request.user.UserLoginRequest;
import org.qualifaizebackendapi.DTO.request.user.UserRegisterRequest;
import org.qualifaizebackendapi.DTO.response.UserAuthResponse;
import org.qualifaizebackendapi.DTO.response.user.UserDetailsResponse;
import org.qualifaizebackendapi.exception.ResourceNotFoundException;
import org.qualifaizebackendapi.model.User;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.UUID;

public interface UserService {
    UserAuthResponse login(UserLoginRequest user);
    UserAuthResponse register(UserRegisterRequest user);
    void deleteUser(UUID userId);
    List<UserDetailsResponse> getAllUsers();
    User fetchUserOrThrow(UUID userId);
    UserDetailsResponse updateUserDetails(UUID userId, UpdateUserDetailsRequest request);
    UserDetailsResponse getCurrentUserDetails();
    /**
     * Promotes a user by adding a new role to their existing roles.
     * Only administrators can perform this operation.
     *
     * @param userId The UUID of the user to promote
     * @param roleString The role to add (as string)
     * @return UserDetailsResponse with updated user information
     * @throws ResourceNotFoundException if the user doesn't exist
     * @throws IllegalArgumentException if the role is invalid or user already has the role
     * @throws AccessDeniedException if the current user is not an admin
     */
    UserDetailsResponse promoteUserRole(UUID userId, String roleString);
}
