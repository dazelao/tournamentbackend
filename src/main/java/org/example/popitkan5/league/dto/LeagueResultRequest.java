package org.example.popitkan5.league.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeagueResultRequest {
    
    @NotNull(message = "ID матча обязательно")
    private Long matchId;
    
    @NotNull(message = "Количество голов игрока 1 обязательно")
    private Integer goalsUser1;
    
    @NotNull(message = "Количество голов игрока 2 обязательно")
    private Integer goalsUser2;
}
