package org.example.popitkan5.dto;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserRequest {
    @Pattern(regexp = "^@?[a-zA-Z0-9_]{1,32}$", message = "Некорректный формат Telegram username")
    private String telegram;
    
    @Pattern(regexp = "^[a-zA-Z0-9_-]{3,16}$", message = "EA ID должен содержать от 3 до 16 символов и может включать только буквы, цифры, подчеркивания и дефисы")
    private String eaId;
}