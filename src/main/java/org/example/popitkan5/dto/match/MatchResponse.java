package org.example.popitkan5.dto.match;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MatchResponse {

    private Long id;

    private Long tournamentId;

    private String player1;

    private String player2;

    private Integer score1;

    private Integer score2;

    private String status;

    private LocalDateTime startTime;

    private LocalDateTime endTime;
}
