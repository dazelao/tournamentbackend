package org.example.popitkan5.Swizz.dto;

import lombok.Data;
import org.example.popitkan5.Swizz.model.SwissTournament;
import org.example.popitkan5.Swizz.model.SwissTournamentStatus;

import java.time.LocalDateTime;

@Data
public class TournamentResponseDto {
    private Long id;
    private String name;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private SwissTournamentStatus status;
    private Integer maxPlayers;
    private Integer currentRound;
    private Integer totalRounds;
    private Integer registeredPlayers;
    private Long winnerId;
    private String winnerUsername;

    public static TournamentResponseDto fromEntity(SwissTournament tournament) {
        TournamentResponseDto dto = new TournamentResponseDto();
        dto.setId(tournament.getId());
        dto.setName(tournament.getName());
        dto.setDescription(tournament.getDescription());
        dto.setStartDate(tournament.getStartDate());
        dto.setEndDate(tournament.getEndDate());
        dto.setStatus(tournament.getStatus());
        dto.setMaxPlayers(tournament.getMaxPlayers());
        dto.setCurrentRound(tournament.getCurrentRound());
        dto.setTotalRounds(tournament.getTotalRounds());
        
        // Считаем активных игроков
        long activeRegistrations = tournament.getRegistrations().stream()
                .filter(reg -> reg.isActive())
                .count();
        dto.setRegisteredPlayers((int) activeRegistrations);
        
        // Информация о победителе, если есть
        if (tournament.getWinner() != null) {
            dto.setWinnerId(tournament.getWinner().getId());
            dto.setWinnerUsername(tournament.getWinner().getUsername());
        }
        
        return dto;
    }
}
