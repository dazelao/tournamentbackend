package org.example.popitkan5.league.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BulkLeagueMatchRequest {
    
    @NotEmpty(message = "Список матчей не может быть пустым")
    @Valid
    private List<MatchData> matches;
    
    @NotNull(message = "ID лиги обязательно")
    private Long leagueId;
    
    private Integer roundNumber;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MatchData {
        @NotNull(message = "ID игрока 1 обязательно")
        private Long userId1;
        
        @NotNull(message = "ID игрока 2 обязательно")
        private Long userId2;
    }
}
