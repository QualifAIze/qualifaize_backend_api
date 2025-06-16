package org.qualifaizebackendapi.repository;

import org.qualifaizebackendapi.model.User;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserRepository extends SoftDeletableRepository<User, UUID> {
    User findByUsername(String username);
}
