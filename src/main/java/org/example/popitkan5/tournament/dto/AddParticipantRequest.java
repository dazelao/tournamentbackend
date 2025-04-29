package org.example.popitkan5.tournament.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddParticipantRequest {
    @NotNull(message = "ID пользователя обязательно")
    private Long userId;
}
