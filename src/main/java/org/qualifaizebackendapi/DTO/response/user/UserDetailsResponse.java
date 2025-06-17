package org.qualifaizebackendapi.DTO.response.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserDetailsResponse extends UserDetailsOverviewResponse{
    private String email;
    private OffsetDateTime memberSince;
    private OffsetDateTime birthDate;
    private boolean isDeleted;
}
