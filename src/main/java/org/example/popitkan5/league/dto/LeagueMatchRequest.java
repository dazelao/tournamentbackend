package org.example.popitkan5.league.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeagueMatchRequest {
    
    @NotNull(message = "ID пользователя 1 обязательно")
    private Long userId1;
    
    @NotNull(message = "ID пользователя 2 обязательно")
    private Long userId2;
    
    @NotNull(message = "ID лиги обязательно")
    private Long leagueId;
    
    private Integer roundNumber;
}
