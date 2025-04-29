package org.example.popitkan5.dto.match;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MatchRequest {

    @NotNull(message = "Идентификатор турнира обязателен")
    private Long tournamentId;

    @NotBlank(message = "Имя первого игрока обязательно")
    private String player1;

    @NotBlank(message = "Имя второго игрока обязательно")
    private String player2;

    private Integer score1;

    private Integer score2;

    private String status = "scheduled";

    @NotNull(message = "Дата и время начала матча обязательны")
    private LocalDateTime startTime;

    private LocalDateTime endTime;
}
