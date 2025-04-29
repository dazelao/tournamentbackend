package org.example.popitkan5.league.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeagueStandingsResponse {
    private Long leagueId;
    private String leagueName;
    private List<StandingEntry> standings;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StandingEntry {
        private Long userId;
        private String username;
        private Integer position;
        private Integer points;
        private Integer matchesPlayed;
        private Integer wins;
        private Integer draws;
        private Integer losses;
        private Integer goalsScored;
        private Integer goalsConceded;
        private Integer goalDifference;
    }
}
