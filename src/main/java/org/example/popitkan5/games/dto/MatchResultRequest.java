package org.example.popitkan5.games.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchResultRequest {

    @NotNull(message = "ID матча обязательно")
    private Long matchId;
    
    private Long tournamentId;
    
    private Integer roundNumber;

    @NotNull(message = "Количество голов игрока 1 обязательно")
    private Integer goalsUser1;

    private Integer goalsUser2;
}
