package org.qualifaizebackendapi.DTO.response;


import org.qualifaizebackendapi.model.Role;

import java.util.Set;

public record RegisteredUserResponse(String username, String token, Set<Role> roles){}