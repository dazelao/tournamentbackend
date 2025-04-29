package org.example.popitkan5.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthRequest {

    @NotBlank(message = "Ник пользователя обязателен")
    private String username;

    @NotBlank(message = "Пароль обязателен")
    private String password;
}
