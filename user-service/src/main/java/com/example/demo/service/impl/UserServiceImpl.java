package com.example.demo.service.impl;

import com.example.demo.aliasGenerator.AliasGenerator;

import com.example.demo.config.JwtAuthenticationConverter;
import com.example.demo.config.TokenUtil;
import com.example.demo.dto.entry.LoginRequest;
import com.example.demo.dto.exit.LoginResponse;
import com.example.demo.exceptions.*;
import com.example.demo.generatorCVU.GeneratorCVU;
import com.example.demo.dto.entry.UserEntryDto;
import com.example.demo.dto.exit.UserRegisterOutDto;
import com.example.demo.entity.User;

import com.example.demo.repository.UserRepository;
import com.example.demo.service.IUserService;
import com.example.demo.utils.JsonPrinter;
import com.example.demo.utils.KeycloakProvider;
import jakarta.ws.rs.core.Response;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class UserServiceImpl implements IUserService {
    private final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private final TokenUtil tokenUtil;


    public UserServiceImpl(UserRepository userRepository, ModelMapper modelMapper, PasswordEncoder passwordEncoder, TokenUtil tokenUtil) {
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.passwordEncoder = passwordEncoder;
        this.tokenUtil= tokenUtil;
        configureMapping();
    }

    /**
     * Método para listar todos los usuarios de Keycloak
     * @return List<UserRepresentation>
     */
    public List<UserRepresentation> findAllUsers(){
        return KeycloakProvider.getRealmResource()
                .users()
                .list();
    }

    /**
     * Método para buscar un usuario por su username
     * @return List<UserRepresentation>
     */
    public List<UserRepresentation> searchUserByUsername(String userName) {
        return KeycloakProvider.getRealmResource()
                .users()
                .searchByUsername(userName, true);
    }

    /**
     * Método para crear un usuario en keycloak y en la base de datos
     * @return String
     */
    public String createUser(@NonNull UserEntryDto userEntryDto) {
        UsersResource usersResource = KeycloakProvider.getUserResource();

        // Verificar si el usuario ya existe en Keycloak basado en el nombre de usuario
        List<UserRepresentation> existingUsers = searchUserByUsername(userEntryDto.getUserName());

        if (!existingUsers.isEmpty()) {
            log.error("User with username {} already exists in Keycloak.", userEntryDto.getUserName());
            return "User already exists in Keycloak!";
        }

        // Verificar si el correo electrónico ya está en uso
        List<UserRepresentation> existingByEmail = KeycloakProvider.getRealmResource()
                .users()
                .search(null, null, null, userEntryDto.getEmail(), null, null);

        if (!existingByEmail.isEmpty()) {
            log.error("User with email {} already exists in Keycloak.", userEntryDto.getEmail());
            return "Email is already registered!";
        }

        // Crear el usuario en Keycloak
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setFirstName(userEntryDto.getFirstName());
        userRepresentation.setLastName(userEntryDto.getLastName());
        userRepresentation.setEmail(userEntryDto.getEmail());
        userRepresentation.setUsername(userEntryDto.getUserName());
        userRepresentation.setEnabled(true);
        userRepresentation.setEmailVerified(true);

        Response response = usersResource.create(userRepresentation);
        int status = response.getStatus();

        if (status == 201) {
            String path = response.getLocation().getPath();
            String userId = path.substring(path.lastIndexOf("/") + 1);

            // Crear credenciales
            CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
            credentialRepresentation.setTemporary(false);
            credentialRepresentation.setType(CredentialRepresentation.PASSWORD);
            credentialRepresentation.setValue(userEntryDto.getPassword());

            usersResource.get(userId).resetPassword(credentialRepresentation);

            // Asignar roles
            RealmResource realmResource = KeycloakProvider.getRealmResource();
            List<RoleRepresentation> rolesRepresentation = getRoleRepresentation(userEntryDto, realmResource);
            realmResource.users().get(userId).roles().realmLevel().add(rolesRepresentation);

            // Generar alias y CVU
            String alias = generateUniqueAlias();
            String cvu = GeneratorCVU.generateCVU(); // Generar CVU único

            // Mapear y guardar el usuario en la base de datos
            User user = modelMapper.map(userEntryDto, User.class);
            user.setAlias(alias);
            user.setCvu(cvu);
            userRepository.save(user);

            // Transformar la entidad obtenida en UserRegisterOutDto
            UserRegisterOutDto userRegisterOutDto = modelMapper.map(user, UserRegisterOutDto.class);
            LOGGER.info("UsuarioRegisterOutDto: " + JsonPrinter.toString(userRegisterOutDto));

            return JsonPrinter.toString(userRegisterOutDto) + " User created successfully!!";

        } else if (status == 409) {
            log.error("User already exists!");
            return "User already exists!";
        } else {
            log.error("Error creating user, please contact the administrator.");
            return "Error creating user, please contact the administrator.";
        }
    }


    /**
     * Método para borrar un usuario en keycloak y en la base de datos
     * @return void
     */
    public void deleteUser(Long userId) {
        // Convertir el Long a String
        String userIdAsString = String.valueOf(userId);
        // Eliminar en Keycloak
        KeycloakProvider.getUserResource().get(userIdAsString).remove();
        // Eliminar en la base de datos
        userRepository.deleteById(userId);
    }

    /**
     * Método para actualizar un usuario en keycloak y en la base de datos
     * @return void
     */
    public void updateUser(Long userId, @NonNull UserEntryDto userEntryDto) {
        // Actualizar en Keycloak
        CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
        credentialRepresentation.setTemporary(false);
        credentialRepresentation.setType(OAuth2Constants.PASSWORD);
        credentialRepresentation.setValue(userEntryDto.getPassword());

        UserRepresentation user = new UserRepresentation();
        user.setUsername(userEntryDto.getUserName());
        user.setFirstName(userEntryDto.getFirstName());
        user.setLastName(userEntryDto.getLastName());
        user.setEmail(userEntryDto.getEmail());
        user.setEnabled(true);
        user.setEmailVerified(true);
        user.setCredentials(Collections.singletonList(credentialRepresentation));

        // Convertir el Long a String
        String userIdAsString = String.valueOf(userId);

        UserResource usersResource = KeycloakProvider.getUserResource().get(userIdAsString);
        usersResource.update(user);

        // Actualizar en la base de datos
        User existingUser = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        modelMapper.map(userEntryDto, existingUser); // Mapear los cambios en la entidad existente
        userRepository.save(existingUser); // Guardar los cambios en la base de datos
    }
    public void verifyUserEmail(String email, String verificationCode) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

        if (user.getVerificationCode() != null && user.getVerificationCode().equals(verificationCode)) {
            user.setEmailVerified(true);
            user.setVerificationCode(null);
            userRepository.save(user);
        } else {
            throw new InvalidVerificationCodeException("Invalid verification code or email.");
        }
    }
    public LoginResponse authenticate(String email, String password) throws UserNotFoundException, IncorrectPasswordException, EmailNotVerifiedException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Usuario inexistente"));
        /*if (!user.isEmailVerified()) {
            throw new EmailNotVerifiedException("Por favor verifica tu correo electrónico utilizando el código que te fue enviado.");
        }
        */
        /*if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IncorrectPasswordException("Contraseña incorrecta");
        }

         */
        // Genera el token
        String token = tokenUtil.generateToken(user.getId(), user.getEmail());
        // Retorna un LoginResponse con userId, email y token
        return new LoginResponse(user.getId(), user.getEmail(), token);
    }

    // Método para generar un alias único a partir de un archivo TXT
    private String generateUniqueAlias() {
        String alias;
        do {
            alias = AliasGenerator.generateAlias();
        } while (userRepository.existsByAlias(alias)); // Verificar si el alias ya existe en la base de datos
        return alias;
    }
    public void logoutUser(String userId) {
        try {
            // Obtener la instancia de Keycloak para realizar la operación de logout
            RealmResource realmResource = KeycloakProvider.getRealmResource();
            UserResource userResource = realmResource.users().get(userId);

            // Revocar sesiones activas del usuario en Keycloak
            userResource.logout();

            LOGGER.info("Logout successful for user with ID: " + userId);
        } catch (Exception e) {
            LOGGER.error("Error during logout for user with ID: " + userId, e);
            throw new RuntimeException("Error during logout. Please try again later.");
        }
    }

    // Obtener la lista de roles
    private List<RoleRepresentation> getRoleRepresentation(UserEntryDto userEntryDto, RealmResource realmResource) {
        List<RoleRepresentation> rolesRepresentation;

        if (userEntryDto.getRoles() == null || userEntryDto.getRoles().isEmpty()) {
            rolesRepresentation = List.of(realmResource.roles().get("user").toRepresentation());
        } else {
            rolesRepresentation = realmResource.roles()
                    .list()
                    .stream()
                    .filter(role -> userEntryDto.getRoles()
                            .stream()
                            .anyMatch(roleName -> roleName.equalsIgnoreCase(role.getName())))
                    .toList();
        }

        return rolesRepresentation;
    }


    // Método para configurar el mapeo de ModelMapper
    private void configureMapping() {
        modelMapper.typeMap(UserEntryDto.class, User.class);
        modelMapper.typeMap(User.class, UserRegisterOutDto.class);
    }
}

