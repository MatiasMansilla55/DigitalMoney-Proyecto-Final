package com.example.userservice.service;

import com.example.userservice.dto.LoginRequest;
import com.example.userservice.exceptions.InvalidPasswordException;
import com.example.userservice.exceptions.UserNotFoundException;
import com.example.userservice.repository.UserRepository;
import org.springframework.stereotype.Service;
import com.example.userservice.dto.User;

@Service
public class UserService {

    private final KeycloakAdminClientService keycloakAdminClient;
    private final UserRepository userRepository;

    public UserService(KeycloakAdminClientService keycloakAdminClient, UserRepository userRepository) {
        this.keycloakAdminClient = keycloakAdminClient;
        this.userRepository = userRepository;
    }

    public String login(LoginRequest loginRequest) throws UserNotFoundException, InvalidPasswordException {
        // Lógica para autenticar con Keycloak y obtener el token JWT
        return keycloakAdminClient.authenticateUser(loginRequest.getEmail(), loginRequest.getPassword());
    }

    public User getUserDetails(Long userId) throws UserNotFoundException {
        // Lógica para obtener los detalles del usuario desde la base de datos MySQL
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));
    }
}
