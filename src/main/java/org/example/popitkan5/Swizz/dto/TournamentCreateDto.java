package org.example.popitkan5.Swizz.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TournamentCreateDto {
    @NotBlank(message = "Название турнира не может быть пустым")
    private String name;

    @NotBlank(message = "Описание турнира не может быть пустым")
    private String description;

    @NotNull(message = "Максимальное количество игроков не может быть пустым")
    @Min(value = 2, message = "Минимальное количество игроков - 2")
    private Integer maxPlayers;

    @NotNull(message = "Дата начала не может быть пустой")
    private LocalDateTime startDate;

    @NotNull(message = "Дата окончания не может быть пустой")
    private LocalDateTime endDate;
}
