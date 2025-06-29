package org.qualifaizebackendapi.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.internal.util.StringUtils;
import org.qualifaizebackendapi.DTO.request.user.UpdateUserDetailsRequest;
import org.qualifaizebackendapi.DTO.request.user.UserLoginRequest;
import org.qualifaizebackendapi.DTO.request.user.UserRegisterRequest;
import org.qualifaizebackendapi.DTO.response.UserAuthResponse;
import org.qualifaizebackendapi.DTO.response.user.UserDetailsOverviewResponse;
import org.qualifaizebackendapi.DTO.response.user.UserDetailsResponse;
import org.qualifaizebackendapi.exception.DuplicateException;
import org.qualifaizebackendapi.exception.ResourceNotFoundException;
import org.qualifaizebackendapi.mapper.UserMapper;
import org.qualifaizebackendapi.model.enums.Role;
import org.qualifaizebackendapi.model.User;
import org.qualifaizebackendapi.repository.UserRepository;
import org.qualifaizebackendapi.security.JwtService;
import org.qualifaizebackendapi.security.QualifAIzeUserDetails;
import org.qualifaizebackendapi.service.UserService;
import org.qualifaizebackendapi.utils.SecurityUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserMapper userMapper;

    public UserAuthResponse register(UserRegisterRequest userRegisterRequestDTO) {
        User user = this.userMapper.toUser(userRegisterRequestDTO);
        user.setPassword(bCryptPasswordEncoder.encode(userRegisterRequestDTO.getPassword()));
        user.setRoles(parseRoles(userRegisterRequestDTO.getRoles()));
        User savedUser = userRepository.save(user);

        String token = jwtService.generateToken(savedUser.getId(), savedUser.getUsername(), user.getRoles());

        return userMapper.toUserAuthResponse(token);
    }

    @Override
    @Transactional
    public void deleteUser(UUID userId) {
        int affectedRowCFromDbRequest = userRepository.softDeleteById(this.fetchUserOrThrow(userId).getId());

        if (affectedRowCFromDbRequest == 0) {
            throw new IllegalArgumentException("User already deleted");
        }
    }

    @Override
    public List<UserDetailsResponse> getAllUsers() {
        return this.userMapper.toUserDetailsResponseList(userRepository.findAllActive());
    }

    public UserAuthResponse login(UserLoginRequest user) {
        final String INCORRECT_CREDENTIALS_MESSAGE = "Username or password is incorrect";
        Authentication authentication;
        try {
             authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()));

            if (!authentication.isAuthenticated()) throw new UsernameNotFoundException(INCORRECT_CREDENTIALS_MESSAGE);

        } catch (UsernameNotFoundException | BadCredentialsException e) {
            throw new UsernameNotFoundException(INCORRECT_CREDENTIALS_MESSAGE);
        } catch (AuthenticationException e) {
            throw new UsernameNotFoundException("Authentication failed: " + e.getMessage());
        }

        QualifAIzeUserDetails userDetails = (QualifAIzeUserDetails) authentication.getPrincipal();
        return new UserAuthResponse(jwtService.generateToken(userDetails.getUser().getId(), userDetails.getUser().getUsername(), userDetails.getUser().getRoles()));
    }

    @Override
    @Transactional
    public UserDetailsResponse updateUserDetails(UUID userId, UpdateUserDetailsRequest request) {
        log.info("Updating user details for user ID: {}", userId);

        SecurityUtils.checkUserAccess(userId);

        User existingUser = fetchUserOrThrow(userId);

        if (StringUtils.hasText(request.getUsername()) &&
                !request.getUsername().equals(existingUser.getUsername())) {

            if (userRepository.existsByUsernameAndIdNot(request.getUsername(), userId)) {
                throw new DuplicateException(
                        String.format("Username '%s' is already taken", request.getUsername())
                );
            }
        }

        if (StringUtils.hasText(request.getEmail()) &&
                !request.getEmail().equals(existingUser.getEmail())) {

            if (userRepository.existsByEmailAndIdNot(request.getEmail(), userId)) {
                throw new DuplicateException(
                        String.format("Email '%s' is already in use", request.getEmail())
                );
            }
        }

        userMapper.updateUserFromRequest(request, existingUser);

        User updatedUser = userRepository.save(existingUser);

        log.info("Successfully updated user details for user ID: {}", userId);

        return userMapper.toUserDetailsResponse(updatedUser);
    }

    @Override
    public UserDetailsResponse getCurrentUserDetails() {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        log.info("Fetching current user details for user ID: {}", currentUserId);

        User currentUser = fetchUserOrThrow(currentUserId);
        return userMapper.toUserDetailsResponse(currentUser);
    }

    @Override
    @Transactional
    public UserDetailsResponse promoteUserRole(UUID userId, String roleString) {
        log.info("Starting role promotion for user ID: {} with role: {}", userId, roleString);

        SecurityUtils.requireAdmin("promote user roles");

        Role roleToAdd = validateAndParseRole(roleString);

        User userToPromote = fetchUserOrThrow(userId);

        if (userToPromote.getRoles().contains(roleToAdd)) {
            throw new IllegalArgumentException(
                    String.format("User '%s' already has role '%s'",
                            userToPromote.getUsername(), roleToAdd.name())
            );
        }

        userToPromote.getRoles().add(roleToAdd);

        User updatedUser = userRepository.save(userToPromote);

        log.info("Successfully promoted user '{}' (ID: {}) with role: {}",
                updatedUser.getUsername(), userId, roleToAdd.name());

        return userMapper.toUserDetailsResponse(updatedUser);
    }


    public User fetchUserOrThrow(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("User with Id %s was now found!", userId)
                ));
    }

    /**
     * Validates and parses the role string into a Role enum.
     *
     * @param roleString The role string to validate
     * @return The parsed Role enum
     * @throws IllegalArgumentException if the role is invalid
     */
    private Role validateAndParseRole(String roleString) {
        if (roleString == null || roleString.trim().isEmpty()) {
            throw new IllegalArgumentException("Role cannot be null or empty");
        }

        try {
            return Role.valueOf(roleString.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            String validRoles = Arrays.stream(Role.values())
                    .map(Role::name)
                    .collect(Collectors.joining(", "));

            throw new IllegalArgumentException(
                    String.format("Invalid role '%s'. Valid roles are: %s",
                            roleString, validRoles)
            );
        }
    }

    private static Set<Role> parseRoles(String[] roles) {
        if (roles == null) {
            roles = new String[]{"GUEST"};
        }

        return Arrays.stream(roles)
                .filter(role -> role != null && !role.isBlank())
                .map(String::trim)
                .map(String::toUpperCase)
                .map(Role::valueOf)
                .collect(Collectors.toSet());
    }
}
