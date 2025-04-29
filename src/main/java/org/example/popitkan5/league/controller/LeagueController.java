package org.example.popitkan5.league.controller;

import jakarta.validation.Valid;
import org.example.popitkan5.league.dto.*;
import org.example.popitkan5.league.service.LeagueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import org.example.popitkan5.model.User;

@RestController
@RequestMapping("/api/leagues")
public class LeagueController {

    private final LeagueService leagueService;

    @Autowired
    public LeagueController(LeagueService leagueService) {
        this.leagueService = leagueService;
    }

    @PostMapping
    public ResponseEntity<LeagueResponse> createLeague(@Valid @RequestBody CreateLeagueRequest request) {
        LeagueResponse league = leagueService.createLeague(request);
        return ResponseEntity.ok(league);
    }

    @GetMapping
    public ResponseEntity<List<LeagueResponse>> getAllLeagues() {
        List<LeagueResponse> leagues = leagueService.getAllLeagues();
        return ResponseEntity.ok(leagues);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LeagueResponse> getLeague(@PathVariable Long id) {
        LeagueResponse league = leagueService.getLeague(id);
        return ResponseEntity.ok(league);
    }

    @PostMapping("/{id}/registration")
    public ResponseEntity<LeagueResponse> startRegistration(@PathVariable Long id) {
        LeagueResponse league = leagueService.startRegistration(id);
        return ResponseEntity.ok(league);
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<LeagueResponse> startLeague(@PathVariable Long id) {
        LeagueResponse league = leagueService.startLeague(id);
        return ResponseEntity.ok(league);
    }

    @PostMapping("/{id}/finish")
    public ResponseEntity<LeagueResponse> finishLeague(@PathVariable Long id) {
        LeagueResponse league = leagueService.finishLeague(id);
        return ResponseEntity.ok(league);
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<LeagueResponse> cancelLeague(@PathVariable Long id) {
        LeagueResponse league = leagueService.cancelLeague(id);
        return ResponseEntity.ok(league);
    }

    @PostMapping("/{id}/participants")
    public ResponseEntity<LeagueResponse> addParticipant(@PathVariable Long id, @Valid @RequestBody ParticipantRequest request) {
        LeagueResponse league = leagueService.addParticipant(id, request.getUserId());
        return ResponseEntity.ok(league);
    }

    @PostMapping("/{id}/participants/bulk")
    public ResponseEntity<LeagueResponse> addParticipantsByAttribute(@PathVariable Long id, @Valid @RequestBody BulkParticipantRequest request) {
        LeagueResponse league = leagueService.addParticipantsByAttribute(id, request);
        return ResponseEntity.ok(league);
    }

    @DeleteMapping("/{id}/participants/{userId}")
    public ResponseEntity<LeagueResponse> removeParticipant(@PathVariable Long id, @PathVariable Long userId) {
        LeagueResponse league = leagueService.removeParticipant(id, userId);
        return ResponseEntity.ok(league);
    }
    
    @GetMapping("/{id}/participants")
    public ResponseEntity<List<User>> getLeagueParticipants(@PathVariable Long id) {
        List<User> participants = leagueService.getLeagueParticipants(id);
        return ResponseEntity.ok(participants);
    }
}
