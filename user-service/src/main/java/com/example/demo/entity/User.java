package com.example.demo.entity;

import com.example.demo.aliasGenerator.AliasGenerator;
import com.example.demo.generatorCVU.GeneratorCVU;
import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "user")
@Getter
@Setter
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(length = 50)
    private String userName;

    @Column(length = 50)
    private String firstName;

    @Column(length = 50)
    private String lastName;

    @Column(length = 50)
    private String dni;  // Cambiado a String

    @Column(length = 50)
    private String phone;  // Cambiado a String

    @Column(length = 50)
    private String email;

    @Column(length = 50)
    private String password;

    // Atributos generados automáticamente
    @Column(length = 22, unique = true)  // CVU es único y tiene un tamaño fijo de 22 caracteres
    private String cvu;

    @Column(length = 300, unique = true)  // Alias es único y tiene un límite de 100 caracteres
    private String alias;
    @Column(length = 300, unique = true)
    private String keycloakId;
    private boolean emailVerified = true;
    private String verificationCode;
    public User() {}

    @PrePersist
    public void generateCVUAndAlias() {
        this.cvu = GeneratorCVU.generateCVU();
        this.alias = AliasGenerator.generateAlias();
    }
}
