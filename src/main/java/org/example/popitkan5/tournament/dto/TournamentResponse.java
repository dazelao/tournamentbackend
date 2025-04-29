package org.example.popitkan5.tournament.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.popitkan5.tournament.model.TournamentStatus;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TournamentResponse {
    private Long id;
    private String name;
    private String description;
    private TournamentStatus status;
    private Integer maxParticipants;
    private LocalDateTime startDate;
    private LocalDateTime modifiedAt;
    private Integer currentRound;
    private Integer participantsCount;
}
