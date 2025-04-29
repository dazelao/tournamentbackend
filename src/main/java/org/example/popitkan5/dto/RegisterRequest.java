package org.example.popitkan5.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.popitkan5.model.Role;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Ник пользователя обязателен")
    private String username;

    private String telegram;

    private String eaId;

    @NotBlank(message = "Пароль обязателен")
    private String password;


    private Role role = Role.USER; // По умолчанию обычный пользователь

    private Map<String, String> attributes = new HashMap<>();
}
