package org.example.popitkan5.league.controller;

import jakarta.validation.Valid;
import org.example.popitkan5.league.dto.BulkLeagueMatchRequest;
import org.example.popitkan5.league.dto.LeagueMatchResponse;
import org.example.popitkan5.league.dto.LeagueResultRequest;
import org.example.popitkan5.league.service.LeagueMatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/league-matches")
public class LeagueMatchController {

    private final LeagueMatchService matchService;

    @Autowired
    public LeagueMatchController(LeagueMatchService matchService) {
        this.matchService = matchService;
    }

    @GetMapping("/my")
    public ResponseEntity<List<LeagueMatchResponse>> getUserMatches(@AuthenticationPrincipal UserDetails userDetails) {
        List<LeagueMatchResponse> matches = matchService.getUserMatches(userDetails.getUsername());
        return ResponseEntity.ok(matches);
    }

    @PostMapping("/generate/{leagueId}")
    public ResponseEntity<List<LeagueMatchResponse>> generateAllMatches(@PathVariable Long leagueId) {
        List<LeagueMatchResponse> matches = matchService.generateAllMatches(leagueId);
        return ResponseEntity.ok(matches);
    }

    @PostMapping("/bulk")
    public ResponseEntity<List<LeagueMatchResponse>> createMatches(@Valid @RequestBody BulkLeagueMatchRequest request) {
        List<LeagueMatchResponse> matches = matchService.createMatches(request);
        return ResponseEntity.ok(matches);
    }

    @GetMapping("/league/{leagueId}")
    public ResponseEntity<List<LeagueMatchResponse>> getMatchesByLeague(@PathVariable Long leagueId) {
        List<LeagueMatchResponse> matches = matchService.getMatchesByLeague(leagueId);
        return ResponseEntity.ok(matches);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LeagueMatchResponse> getMatch(@PathVariable Long id) {
        LeagueMatchResponse match = matchService.getMatch(id);
        return ResponseEntity.ok(match);
    }

    @PostMapping("/{id}/result")
    public ResponseEntity<LeagueMatchResponse> updateMatchResult(@PathVariable Long id, @Valid @RequestBody LeagueResultRequest request) {
        LeagueMatchResponse match = matchService.updateMatchResult(id, request);
        return ResponseEntity.ok(match);
    }
}
