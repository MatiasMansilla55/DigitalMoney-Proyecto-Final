package com.example.userservice.service;

import com.example.userservice.dto.KeycloakTokenResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import com.example.userservice.exceptions.UserNotFoundException;
import com.example.userservice.exceptions.InvalidPasswordException;

@Service
public class KeycloakAdminClientService {

    @Autowired
    private RestTemplate restTemplate;

    public String authenticateUser(String email, String password) throws UserNotFoundException, InvalidPasswordException {
        // Construcción de solicitud a Keycloak
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_id", "<keycloak-client-id>");
        formData.add("username", email);
        formData.add("password", password);
        formData.add("grant_type", "password");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formData, headers);

        try {
            ResponseEntity<KeycloakTokenResponse> response = restTemplate.postForEntity(
                    "http://localhost:8080/realms/dh/protocol/openid-connect/token",
                    request,
                    KeycloakTokenResponse.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody().getAccessToken();
            } else {
                throw new InvalidPasswordException("Contraseña incorrecta");
            }

        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new UserNotFoundException("Usuario inexistente");
            }
            throw new InvalidPasswordException("Contraseña incorrecta");
        } catch (Exception e) {
            throw new RuntimeException("Error al autenticar con Keycloak");
        }
    }
}