package org.qualifaizebackendapi.DTO.db_object;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserDetailsRow extends UserDetailsOverviewRow {
    private String email;
    private OffsetDateTime memberSince;
    private OffsetDateTime birthDate;
    private boolean isDeleted;
}
