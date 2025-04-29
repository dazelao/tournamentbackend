package org.example.popitkan5.games.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkCreateMatchesRequest {
    
    @NotEmpty(message = "Список матчей не может быть пустым")
    @Valid
    private List<MatchData> matches;
    
    private Long tournamentId;
    
    private Integer roundNumber;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MatchData {
        @NotNull(message = "ID игрока 1 обязательно")
        private Long userId1;
        
        private Long userId2;
    }
}
