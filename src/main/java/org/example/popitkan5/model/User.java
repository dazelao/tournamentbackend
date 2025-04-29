package org.example.popitkan5.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username; // Ник пользователя

    @Column(unique = true)
    private String telegram; // Телеграм аккаунт

    @Column(name = "ea_id", unique = true)
    private String eaId; // EA ID

    @JsonIgnore
    @Column(nullable = false)
    private String password; // Пароль пользователя

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role; // Роль пользователя

    @ElementCollection
    @CollectionTable(name = "user_attributes", joinColumns = @JoinColumn(name = "user_id"))
    @MapKeyColumn(name = "key")
    @Column(name = "value")
    private Map<String, String> attributes = new HashMap<>(); // Дополнительные атрибуты
}
