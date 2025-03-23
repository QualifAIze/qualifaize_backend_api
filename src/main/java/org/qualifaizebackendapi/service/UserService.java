package org.qualifaizebackendapi.service;

import lombok.RequiredArgsConstructor;
import org.qualifaizebackendapi.DTO.request.UserLoginRequest;
import org.qualifaizebackendapi.DTO.request.UserRegisterRequest;
import org.qualifaizebackendapi.DTO.response.RegisteredUserResponse;
import org.qualifaizebackendapi.DTO.response.UserLoginResponse;
import org.qualifaizebackendapi.mapper.UserMapper;
import org.qualifaizebackendapi.model.Role;
import org.qualifaizebackendapi.model.User;
import org.qualifaizebackendapi.repository.UserRepository;
import org.qualifaizebackendapi.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserMapper userMapper;

    public RegisteredUserResponse register(UserRegisterRequest userRegisterRequestDTO) {
        User user = new User();
        user.setUsername(userRegisterRequestDTO.username());
        user.setPassword(bCryptPasswordEncoder.encode(userRegisterRequestDTO.password()));
        user.setRoles(parseRoles(userRegisterRequestDTO.roles()));
        User savedUser = userRepository.save(user);
        String token = jwtService.generateToken(savedUser.getUsername());
        return userMapper.userToRegisteredUserResponse(savedUser, token);
    }

    public UserLoginResponse verify(UserLoginRequest user){
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user.username(), user.password()));

        if (!authentication.isAuthenticated()) {
            throw new UsernameNotFoundException("Username or password is incorrect");
        }

        return new UserLoginResponse(jwtService.generateToken(user.username()));
    }

    public static Set<Role> parseRoles(String[] roles) {
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
