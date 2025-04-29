package org.example.popitkan5.tournament.controller;

import lombok.RequiredArgsConstructor;
import org.example.popitkan5.tournament.dto.TournamentResponse;
import org.example.popitkan5.tournament.service.UserTournamentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user-tournaments")
@RequiredArgsConstructor
public class UserTournamentController {

    private final UserTournamentService userTournamentService;

    /**
     * Получает список всех турниров, в которых участвует текущий пользователь
     */
    @GetMapping("/me")
    public ResponseEntity<List<TournamentResponse>> getCurrentUserTournaments() {
        return ResponseEntity.ok(userTournamentService.getCurrentUserTournaments());
    }

    /**
     * Получает список всех турниров, в которых участвует указанный пользователь
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TournamentResponse>> getUserTournaments(@PathVariable Long userId) {
        return ResponseEntity.ok(userTournamentService.getUserTournaments(userId));
    }
}
