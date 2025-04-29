package org.example.popitkan5.Swizz.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MatchUpdateDto {
    @NotNull(message = "ID матча не может быть пустым")
    private Long matchId;

    @NotNull(message = "Счет первого игрока не может быть пустым")
    @Min(value = 0, message = "Счет не может быть отрицательным")
    private Integer player1Score;

    @NotNull(message = "Счет второго игрока не может быть пустым")
    @Min(value = 0, message = "Счет не может быть отрицательным")
    private Integer player2Score;
}
