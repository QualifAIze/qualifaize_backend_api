package org.qualifaizebackendapi.repository;

import org.qualifaizebackendapi.model.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserRepository extends SoftDeletableRepository<User, UUID> {
    User findByUsername(String username);

    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.username = :username AND u.id != :userId AND u.deleted = false")
    boolean existsByUsernameAndIdNot(@Param("username") String username, @Param("userId") UUID userId);

    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.email = :email AND u.id != :userId AND u.deleted = false")
    boolean existsByEmailAndIdNot(@Param("email") String email, @Param("userId") UUID userId);
}