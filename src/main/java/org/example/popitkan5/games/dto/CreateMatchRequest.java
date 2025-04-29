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
public class CreateMatchRequest {

    @NotNull(message = "ID игрока 1 обязательно")
    private Long userId1;

    @NotNull(message = "ID игрока 2 обязательно")
    private Long userId2;
    
    private Long tournamentId;
    
    private Integer roundNumber;
}
