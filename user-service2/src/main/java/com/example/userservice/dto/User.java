package com.example.userservice.dto;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
@Entity
@Getter
@Setter
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userName;


    private String firstName;


    private String lastName;


    private String dni;  // Cambiado a String


    private String phone;  // Cambiado a String


    private String email;


    private String password;


    private String cvu;


    private String alias;

    private String keycloakId;
}
