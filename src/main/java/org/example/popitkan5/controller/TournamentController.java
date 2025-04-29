package org.example.popitkan5.controller;

import org.example.popitkan5.dto.tournament.TournamentResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * Временный контроллер для имитации работы с турнирами
 * В будущем будет заменен полноценной реализацией
 */
@RestController
@RequestMapping("/api/tournaments")
public class TournamentController {

    @GetMapping
    public ResponseEntity<List<TournamentResponse>> getAllTournaments() {
        // Временная заглушка для списка турниров
        List<TournamentResponse> tournaments = new ArrayList<>();
        
        // Добавляем несколько тестовых турниров
        TournamentResponse fifa = TournamentResponse.builder()
            .id(1L)
            .name("FIFA 2025 Championship")
            .status("active")
            .build();
        
        TournamentResponse eaSports = TournamentResponse.builder()
            .id(2L)
            .name("EA Sports Cup")
            .status("planned")
            .build();
            
        TournamentResponse donChamps = TournamentResponse.builder()
            .id(3L)
            .name("DonChamps League")
            .status("active")
            .build();
        
        tournaments.add(fifa);
        tournaments.add(eaSports);
        tournaments.add(donChamps);
        
        return ResponseEntity.ok(tournaments);
    }
}
