package org.example.popitkan5.league.controller;

import org.example.popitkan5.league.dto.LeagueStandingsResponse;
import org.example.popitkan5.league.service.LeagueStatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/league-stats")
public class LeagueStatsController {

    private final LeagueStatsService statsService;

    @Autowired
    public LeagueStatsController(LeagueStatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/{leagueId}")
    public ResponseEntity<LeagueStandingsResponse> getLeagueStandings(@PathVariable Long leagueId) {
        LeagueStandingsResponse standings = statsService.getLeagueStandings(leagueId);
        return ResponseEntity.ok(standings);
    }

    @PostMapping("/{leagueId}/recalculate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> recalculatePositions(@PathVariable Long leagueId) {
        statsService.recalculatePositions(leagueId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{leagueId}/distribute-attributes")
    public ResponseEntity<Void> distributeAttributes(@PathVariable Long leagueId) {
        statsService.distributeAttributes(leagueId);
        return ResponseEntity.ok().build();
    }
}
