package org.qualifaizebackendapi.DTO.request;

public record UserRegisterRequest(String username, String password, String[] roles) {
}
