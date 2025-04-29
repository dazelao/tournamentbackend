package org.example.popitkan5.league.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantRequest {
    
    @NotNull(message = "ID пользователя обязательно")
    private Long userId;
}
