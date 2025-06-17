package org.qualifaizebackendapi.DTO.response;


import org.qualifaizebackendapi.model.enums.Role;

import java.util.Set;

public record UserRegisterResponse(String username, String token, Set<Role> roles){}