package org.example.popitkan5.tournament.controller;

import jakarta.validation.Valid;
import org.example.popitkan5.model.User;
import org.example.popitkan5.tournament.dto.AddParticipantRequest;
import org.example.popitkan5.tournament.dto.CreateTournamentRequest;
import org.example.popitkan5.tournament.dto.TournamentResponse;
import org.example.popitkan5.tournament.model.TournamentStatus;
import org.example.popitkan5.tournament.service.TournamentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tournament")
public class SingleEliminationTournamentController {

    private final TournamentService tournamentService;

    @Autowired
    public SingleEliminationTournamentController(TournamentService tournamentService) {
        this.tournamentService = tournamentService;
    }

    /**
     * Создает новый турнир
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TournamentResponse> createTournament(@Valid @RequestBody CreateTournamentRequest request) {
        TournamentResponse tournament = tournamentService.createTournament(request);
        return ResponseEntity.ok(tournament);
    }

    /**
     * Получает турнир по ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<TournamentResponse> getTournament(@PathVariable Long id) {
        TournamentResponse tournament = tournamentService.getTournamentById(id);
        return ResponseEntity.ok(tournament);
    }

    /**
     * Получает все турниры
     */
    @GetMapping
    public ResponseEntity<List<TournamentResponse>> getAllTournaments() {
        List<TournamentResponse> tournaments = tournamentService.getAllTournaments();
        return ResponseEntity.ok(tournaments);
    }

    /**
     * Получает турниры по статусу
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<TournamentResponse>> getTournamentsByStatus(@PathVariable TournamentStatus status) {
        List<TournamentResponse> tournaments = tournamentService.getTournamentsByStatus(status);
        return ResponseEntity.ok(tournaments);
    }

    /**
     * Добавляет участника в турнир
     */
    @PostMapping("/{id}/participant")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TournamentResponse> addParticipant(
            @PathVariable Long id,
            @Valid @RequestBody AddParticipantRequest request) {
        TournamentResponse tournament = tournamentService.addParticipant(id, request);
        return ResponseEntity.ok(tournament);
    }

    /**
     * Удаляет участника из турнира
     */
    @DeleteMapping("/{tournamentId}/participant/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TournamentResponse> removeParticipant(
            @PathVariable Long tournamentId,
            @PathVariable Long userId) {
        TournamentResponse tournament = tournamentService.removeParticipant(tournamentId, userId);
        return ResponseEntity.ok(tournament);
    }

    /**
     * Запускает турнир
     */
    @PostMapping("/start/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TournamentResponse> startTournament(@PathVariable Long id) {
        TournamentResponse tournament = tournamentService.startTournament(id);
        return ResponseEntity.ok(tournament);
    }

    /**
     * Генерирует следующий раунд турнира
     */
    @PostMapping("/next-round/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TournamentResponse> generateNextRound(@PathVariable Long id) {
        TournamentResponse tournament = tournamentService.generateNextRound(id);
        return ResponseEntity.ok(tournament);
    }

    /**
     * Завершает турнир
     */
    @PostMapping("/finish/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TournamentResponse> finishTournament(@PathVariable Long id) {
        TournamentResponse tournament = tournamentService.finishTournament(id);
        return ResponseEntity.ok(tournament);
    }

    /**
     * Получает список участников турнира
     */
    @GetMapping("/{id}/participants")
    public ResponseEntity<List<User>> getTournamentParticipants(@PathVariable Long id) {
        List<User> participants = tournamentService.getTournamentParticipants(id);
        return ResponseEntity.ok(participants);
    }
}
