package org.example.popitkan5.games.controller;

import jakarta.validation.Valid;
import org.example.popitkan5.games.dto.BulkCreateMatchesRequest;
import org.example.popitkan5.games.dto.CreateMatchRequest;
import org.example.popitkan5.games.dto.MatchResponse;
import org.example.popitkan5.games.dto.MatchResultRequest;
import org.example.popitkan5.games.service.MatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/matches")
public class MatchController {

    private final MatchService matchService;

    @Autowired
    public MatchController(MatchService matchService) {
        this.matchService = matchService;
    }

    /**
     * Создает новый матч
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MatchResponse> createMatch(@Valid @RequestBody CreateMatchRequest request) {
        MatchResponse match = matchService.createMatch(request);
        return ResponseEntity.ok(match);
    }
    
    /**
     * Массовое создание матчей
     */
    @PostMapping("/bulk")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<MatchResponse>> createMatches(@Valid @RequestBody BulkCreateMatchesRequest request) {
        List<MatchResponse> matches = matchService.createMatches(request);
        return ResponseEntity.ok(matches);
    }

    /**
     * Получает матч по ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<MatchResponse> getMatch(@PathVariable Long id) {
        MatchResponse match = matchService.getMatchById(id);
        return ResponseEntity.ok(match);
    }

    /**
     * Обновляет результат матча
     */
    @PutMapping("/result")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MatchResponse> updateMatchResult(@Valid @RequestBody MatchResultRequest request) {
        MatchResponse updatedMatch = matchService.updateMatchResult(request);
        return ResponseEntity.ok(updatedMatch);
    }

    /**
     * Получает все матчи для турнира
     */
    @GetMapping("/tournament/{tournamentId}")
    public ResponseEntity<List<MatchResponse>> getMatchesByTournament(@PathVariable Long tournamentId) {
        List<MatchResponse> matches = matchService.getMatchesByTournament(tournamentId);
        return ResponseEntity.ok(matches);
    }

    /**
     * Получает все матчи для турнира и связанных пользователей
     */
    @GetMapping("/tournament/{tournamentId}/with-users")
    public ResponseEntity<Map<String, Object>> getMatchesWithUsersByTournament(@PathVariable Long tournamentId) {
        Map<String, Object> result = matchService.getMatchesWithUsersByTournament(tournamentId);
        return ResponseEntity.ok(result);
    }

    /**
     * Получает все матчи для турнира и раунда
     */
    @GetMapping("/tournament/{tournamentId}/round/{roundNumber}")
    public ResponseEntity<List<MatchResponse>> getMatchesByTournamentAndRound(
            @PathVariable Long tournamentId,
            @PathVariable Integer roundNumber) {
        List<MatchResponse> matches = matchService.getMatchesByTournamentAndRound(tournamentId, roundNumber);
        return ResponseEntity.ok(matches);
    }

    /**
     * Получает все матчи для игрока
     */
    @GetMapping("/player/{userId}")
    public ResponseEntity<List<MatchResponse>> getMatchesByPlayer(@PathVariable Long userId) {
        List<MatchResponse> matches = matchService.getMatchesByPlayer(userId);
        return ResponseEntity.ok(matches);
    }
}