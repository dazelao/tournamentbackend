package org.example.popitkan5.tournament.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTournamentRequest {
    @NotBlank(message = "Название турнира обязательно")
    private String name;
    
    private String description;
    
    @NotNull(message = "Максимальное количество участников обязательно")
    @Min(value = 2, message = "Минимальное количество участников: 2")
    @Max(value = 256, message = "Максимальное количество участников: 256")
    private Integer maxParticipants;
}
