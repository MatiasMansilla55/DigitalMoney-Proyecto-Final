package com.example.demo.service;

import com.example.demo.dto.entry.UserEntryDto;
import com.example.demo.dto.exit.LoginResponse;
import com.example.demo.exceptions.EmailNotVerifiedException;
import com.example.demo.exceptions.IncorrectPasswordException;
import com.example.demo.exceptions.UserNotFoundException;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.List;

public interface IUserService {
    List<UserRepresentation> findAllUsers();
    List<UserRepresentation> searchUserByUsername(String userName);
    String createUser(UserEntryDto userEntryDto);
    void deleteUser(Long userId);
    void updateUser(Long userId, UserEntryDto userEntryDto);

    void logoutUser(String token);
    public LoginResponse authenticate(String email, String password) throws UserNotFoundException, IncorrectPasswordException, EmailNotVerifiedException;
    public void verifyUserEmail(String email, String verificationCode);
}
