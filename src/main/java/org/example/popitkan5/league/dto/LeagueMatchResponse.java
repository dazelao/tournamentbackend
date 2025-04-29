package org.example.popitkan5.league.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeagueMatchResponse {
    private Long id;
    private Long userId1;
    private Long userId2;
    private String username1;
    private String username2;
    private Integer goalsUser1;
    private Integer goalsUser2;
    private String resultUser1;
    private String resultUser2;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private Long leagueId;
    private Integer roundNumber;
    private Long winnerId;
    private Long loserId;
    private Boolean isDraw;
}
