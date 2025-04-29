package org.example.popitkan5.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * DTO для передачи данных о пользователе
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    
    private Long id;
    private String username;
    // email поле не используется в модели User
    private String telegram;
    private String eaId;
    private String role;
    private Map<String, String> attributes = new HashMap<>();
}