package org.example.popitkan5.Swizz.dto;

import lombok.Data;
import org.example.popitkan5.Swizz.model.MatchStatus;
import org.example.popitkan5.Swizz.model.SwissMatch;

import java.time.LocalDateTime;

@Data
public class MatchResponseDto {
    private Long id;
    private Long tournamentId;
    private Long player1Id;
    private String player1Username;
    private Long player2Id;
    private String player2Username;
    private Integer round;
    private Integer player1Score;
    private Integer player2Score;
    private MatchStatus status;
    private LocalDateTime scheduledTime;
    private LocalDateTime completedTime;
    private String matchUrl;
    private boolean isBye;

    public static MatchResponseDto fromEntity(SwissMatch match) {
        MatchResponseDto dto = new MatchResponseDto();
        dto.setId(match.getId());
        dto.setTournamentId(match.getTournament().getId());
        
        // Check player 1 existence
        if (match.getPlayer1() != null) {
            dto.setPlayer1Id(match.getPlayer1().getId());
            dto.setPlayer1Username(match.getPlayer1().getUsername());
        } // If player1 is null, ID and Username remain null in DTO
        
        // Check player 2 existence
        if (match.getPlayer2() != null) {
            dto.setPlayer2Id(match.getPlayer2().getId());
            dto.setPlayer2Username(match.getPlayer2().getUsername());
            dto.setBye(false);
        } else {
            dto.setBye(true);
        }
        
        dto.setRound(match.getRound());
        dto.setPlayer1Score(match.getPlayer1Score());
        dto.setPlayer2Score(match.getPlayer2Score());
        dto.setStatus(match.getStatus());
        dto.setScheduledTime(match.getScheduledTime());
        dto.setCompletedTime(match.getCompletedTime());
        dto.setMatchUrl(match.getMatchUrl());
        
        return dto;
    }
}
