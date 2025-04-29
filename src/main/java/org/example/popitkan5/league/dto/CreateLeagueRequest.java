package org.example.popitkan5.league.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateLeagueRequest {
    
    @NotBlank(message = "Название лиги обязательно")
    private String name;
    
    @NotNull(message = "Максимальное количество участников обязательно")
    @Min(value = 2, message = "Минимальное количество участников - 2")
    private Integer maxParticipants;
    
    // Опциональные поля для атрибутов
    private String winnerAttribute;
    private String loserAttribute;
    private String saveAttribute;
    
    private Integer winnerCount;
    private Integer loserCount;
    private Integer saveCount;
}
