package com.example.demo.repository;



import com.example.demo.dto.entry.UserEntryDto;
import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User>findByEmail(String email);
    boolean existsByAlias(String alias); // Verificar si un alias ya existe
    //Optional<User>findByName(String userName);


}
