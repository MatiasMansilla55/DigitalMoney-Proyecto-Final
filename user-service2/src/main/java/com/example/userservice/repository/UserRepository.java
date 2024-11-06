package com.example.userservice.repository;

import com.example.userservice.dto.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    @NonNull
    Optional<User>findById(@NonNull Long id);
}
