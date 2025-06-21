package org.qualifaizebackendapi.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.qualifaizebackendapi.DTO.request.UserLoginRequest;
import org.qualifaizebackendapi.DTO.request.UserRegisterRequest;
import org.qualifaizebackendapi.DTO.response.UserAuthResponse;
import org.qualifaizebackendapi.exception.ResourceNotFoundException;
import org.qualifaizebackendapi.mapper.UserMapper;
import org.qualifaizebackendapi.model.enums.Role;
import org.qualifaizebackendapi.model.User;
import org.qualifaizebackendapi.repository.UserRepository;
import org.qualifaizebackendapi.security.JwtService;
import org.qualifaizebackendapi.security.QualifAIzeUserDetails;
import org.qualifaizebackendapi.service.UserService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
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

        String token = jwtService.generateToken(savedUser.getUsername(), user.getRoles());

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
        return new UserAuthResponse(jwtService.generateToken(userDetails.getUser().getUsername(), userDetails.getUser().getRoles()));
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

    public User fetchUserOrThrow(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("User with Id %s was now found!", userId)
                ));
    }
}
