package org.qualifaizebackendapi.DTO.response.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.qualifaizebackendapi.model.enums.Role;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserDetailsResponse extends UserDetailsOverviewResponse{
    private String email;
    private OffsetDateTime memberSince;
    private OffsetDateTime birthDate;
    List<Role> roles;
}
