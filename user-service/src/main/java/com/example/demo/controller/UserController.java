package com.example.demo.controller;

import com.example.demo.dto.entry.LoginRequest;
import com.example.demo.dto.entry.UserEntryDto;
import com.example.demo.dto.entry.VerificationRequest;
import com.example.demo.dto.exit.LoginResponse;
import com.example.demo.service.IUserService;
import com.example.demo.service.impl.TokenBlacklistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
@RestController
@RequestMapping("/users")
//@PreAuthorize("hasRole('client_admin')")
public class UserController {
    @Autowired
    private IUserService userService;

    private final TokenBlacklistService tokenBlacklistService;

    public UserController(IUserService userService, TokenBlacklistService tokenBlacklistService) {
        this.userService = userService;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @GetMapping("/search")
    public ResponseEntity<?> findAllUsers(){
        return ResponseEntity.ok(userService.findAllUsers());
    }


    @GetMapping("/search/{userName}")
    public ResponseEntity<?> searchUserByUsername(@PathVariable String userName){
        return ResponseEntity.ok(userService.searchUserByUsername(userName));
    }
    @PostMapping("/verify")
    public ResponseEntity<?> verifyUserEmail(@RequestBody VerificationRequest verificationRequest) {
        userService.verifyUserEmail(verificationRequest.getEmail(), verificationRequest.getVerificationCode());
        return ResponseEntity.ok("Email verified successfully.");
    }


    @PostMapping("/register")
    public ResponseEntity<?> createUser(@RequestBody UserEntryDto userEntryDto) throws URISyntaxException {
        String response = userService.createUser(userEntryDto);
        return ResponseEntity.created(new URI("/keycloak/user/create")).body(response);
    }
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        LoginResponse loginResponse = userService.authenticate(loginRequest.getEmail(), loginRequest.getPassword());
        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/{userId}/logout")
    public ResponseEntity<String> logoutUser(@PathVariable String userId) {
        try {
            userService.logoutUser(userId);
            return ResponseEntity.ok("Logout successful");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error during logout: " + e.getMessage());
        }

    }
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String token) {
        try {
            // Elimina el prefijo "Bearer " si está presente
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            tokenBlacklistService.invalidateToken(token);
            return ResponseEntity.ok("Token invalidado correctamente.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al invalidar el token.");
        }
    }
    @PutMapping("/update/{userId}")
    public ResponseEntity<?> updateUser(@PathVariable Long userId, @RequestBody UserEntryDto userEntryDto){
        userService.updateUser(userId, userEntryDto);
        return ResponseEntity.ok("User updated successfully");
    }


    @DeleteMapping("/delete/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId){
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}
