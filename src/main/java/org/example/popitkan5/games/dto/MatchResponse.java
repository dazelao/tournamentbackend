package org.example.popitkan5.games.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchResponse {
    private Long id;
    private Long userId1;
    private Long userId2;
    private Integer goalsUser1;
    private Integer goalsUser2;
    private String resultUser1;
    private String resultUser2;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private Long tournamentId;
    private Integer roundNumber;
    private Long winnerId;
    private Long loserId;
    private Long drawUser1;
    private Long drawUser2;
    private String matchStatus;
}
