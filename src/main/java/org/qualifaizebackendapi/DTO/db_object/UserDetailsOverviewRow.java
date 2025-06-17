package org.qualifaizebackendapi.DTO.db_object;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserDetailsOverviewRow {
    private UUID userId;
    private String username;
    private String firstName;
    private String lastName;
}
