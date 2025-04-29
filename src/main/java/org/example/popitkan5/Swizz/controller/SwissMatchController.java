package org.example.popitkan5.Swizz.controller;

import lombok.RequiredArgsConstructor;
import org.example.popitkan5.Swizz.dto.MatchResponseDto;
import org.example.popitkan5.Swizz.dto.MatchUpdateDto;
import org.example.popitkan5.Swizz.model.MatchStatus;
import org.example.popitkan5.Swizz.model.SwissMatch;
import org.example.popitkan5.Swizz.repository.SwissMatchRepository;
import org.example.popitkan5.Swizz.service.SwissTournamentService;
import org.example.popitkan5.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/swiss/matches")
@RequiredArgsConstructor
public class SwissMatchController {
    private final SwissMatchRepository matchRepository;
    private final SwissTournamentService tournamentService;

    /**
     * Получает информацию о матче
     * @param matchId ID матча
     * @return информация о матче
     */
    @GetMapping("/{matchId}")
    public ResponseEntity<MatchResponseDto> getMatch(@PathVariable Long matchId) {
        SwissMatch match = matchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Match not found"));
        return ResponseEntity.ok(MatchResponseDto.fromEntity(match));
    }

    /**
     * Получает список матчей текущего пользователя
     * @param user текущий пользователь
     * @return список матчей
     */
    @GetMapping("/my")
    public ResponseEntity<List<MatchResponseDto>> getMyMatches(@AuthenticationPrincipal User user) {
        List<SwissMatch> matches = matchRepository.findAll().stream()
                .filter(match -> match.getPlayer1().getId().equals(user.getId()) || 
                        (match.getPlayer2() != null && match.getPlayer2().getId().equals(user.getId())))
                .toList();
        
        List<MatchResponseDto> response = matches.stream()
                .map(MatchResponseDto::fromEntity)
                .toList();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Получает список предстоящих матчей текущего пользователя
     * @param user текущий пользователь
     * @return список матчей
     */
    @GetMapping("/my/upcoming")
    public ResponseEntity<List<MatchResponseDto>> getMyUpcomingMatches(@AuthenticationPrincipal User user) {
        List<SwissMatch> matches = matchRepository.findAll().stream()
                .filter(match -> (match.getPlayer1().getId().equals(user.getId()) || 
                        (match.getPlayer2() != null && match.getPlayer2().getId().equals(user.getId()))) &&
                        (match.getStatus() == MatchStatus.SCHEDULED || match.getStatus() == MatchStatus.IN_PROGRESS))
                .toList();
        
        List<MatchResponseDto> response = matches.stream()
                .map(MatchResponseDto::fromEntity)
                .toList();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Обновляет статус матча на "В процессе"
     * @param matchId ID матча
     * @return статус операции
     */
    @PostMapping("/{matchId}/start")
    public ResponseEntity<String> startMatch(@PathVariable Long matchId) {
        SwissMatch match = matchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Match not found"));
        
        if (match.getStatus() != MatchStatus.SCHEDULED) {
            return ResponseEntity.badRequest().body("Match is not in scheduled state");
        }
        
        match.setStatus(MatchStatus.IN_PROGRESS);
        matchRepository.save(match);
        
        return ResponseEntity.ok("Match started successfully");
    }

    /**
     * Обновляет результат матча
     * @param matchId ID матча
     * @param dto данные для обновления
     * @return статус операции
     */
    @PostMapping("/{matchId}/update")
    public ResponseEntity<String> updateMatch(
            @PathVariable Long matchId,
            @Valid @RequestBody MatchUpdateDto dto) {
        
        SwissMatch match = matchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Match not found"));
        
        tournamentService.updateMatchResult(
                match.getTournament().getId(), 
                matchId, 
                dto.getPlayer1Score(), 
                dto.getPlayer2Score());
        
        return ResponseEntity.ok("Match result updated successfully");
    }

    /**
     * Отменяет матч
     * @param matchId ID матча
     * @return статус операции
     */
    @PostMapping("/{matchId}/cancel")
    public ResponseEntity<String> cancelMatch(@PathVariable Long matchId) {
        SwissMatch match = matchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Match not found"));
        
        if (match.getStatus() == MatchStatus.COMPLETED || match.getStatus() == MatchStatus.CANCELLED) {
            return ResponseEntity.badRequest().body("Cannot cancel completed or already cancelled match");
        }
        
        match.setStatus(MatchStatus.CANCELLED);
        matchRepository.save(match);
        
        return ResponseEntity.ok("Match cancelled successfully");
    }
}
