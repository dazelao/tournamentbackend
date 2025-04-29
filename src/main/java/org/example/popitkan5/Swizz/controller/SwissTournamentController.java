package org.example.popitkan5.Swizz.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.popitkan5.Swizz.dto.*;
import org.example.popitkan5.Swizz.model.PlayerResult;
import org.example.popitkan5.Swizz.model.SwissMatch;
import org.example.popitkan5.Swizz.model.SwissRegistration;
import org.example.popitkan5.Swizz.model.SwissTournament;
import org.example.popitkan5.Swizz.service.SwissTournamentService;
import org.example.popitkan5.model.User;
import org.example.popitkan5.repository.UserRepository;
import org.example.popitkan5.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/swiss")
@RequiredArgsConstructor
public class SwissTournamentController {
    private final SwissTournamentService tournamentService;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private static final Logger log = LoggerFactory.getLogger(SwissTournamentController.class);

    /**
     * Создает новый турнир
     * @param dto данные для создания турнира
     * @return информация о созданном турнире
     */
    @PostMapping("/tournaments")
    public ResponseEntity<TournamentResponseDto> createTournament(@Valid @RequestBody TournamentCreateDto dto) {
        SwissTournament tournament = tournamentService.createTournament(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(TournamentResponseDto.fromEntity(tournament));
    }

    /**
     * Получает информацию о турнире
     * @param tournamentId ID турнира
     * @return информация о турнире
     */
    @GetMapping("/tournaments/{tournamentId}")
    public ResponseEntity<TournamentResponseDto> getTournament(@PathVariable Long tournamentId) {
        SwissTournament tournament = tournamentService.getTournamentById(tournamentId);
        return ResponseEntity.ok(TournamentResponseDto.fromEntity(tournament));
    }

    /**
     * Получает список всех турниров
     * @return список турниров
     */
    @GetMapping("/tournaments")
    public ResponseEntity<List<TournamentResponseDto>> getAllTournaments() {
        List<SwissTournament> tournaments = tournamentService.getAllTournaments();
        List<TournamentResponseDto> response = tournaments.stream()
                .map(TournamentResponseDto::fromEntity)
                .toList();
        return ResponseEntity.ok(response);
    }

    /**
     * Получает список активных турниров
     * @return список активных турниров
     */
    @GetMapping("/tournaments/active")
    public ResponseEntity<List<TournamentResponseDto>> getActiveTournaments() {
        List<SwissTournament> tournaments = tournamentService.getActiveTournaments();
        List<TournamentResponseDto> response = tournaments.stream()
                .map(TournamentResponseDto::fromEntity)
                .toList();
        return ResponseEntity.ok(response);
    }

    /**
     * Получает список турниров с открытой регистрацией
     * @return список турниров с открытой регистрацией
     */
    @GetMapping("/tournaments/registration")
    public ResponseEntity<List<TournamentResponseDto>> getOpenRegistrationTournaments() {
        List<SwissTournament> tournaments = tournamentService.getOpenRegistrationTournaments();
        List<TournamentResponseDto> response = tournaments.stream()
                .map(TournamentResponseDto::fromEntity)
                .toList();
        return ResponseEntity.ok(response);
    }

    /**
     * Открывает регистрацию на турнир
     * @param tournamentId ID турнира
     * @return статус операции
     */
    @PostMapping("/tournaments/{tournamentId}/open")
    public ResponseEntity<String> openRegistration(@PathVariable Long tournamentId) {
        tournamentService.openRegistration(tournamentId);
        return ResponseEntity.ok("Registration opened successfully");
    }

    /**
     * Закрывает регистрацию на турнир
     * @param tournamentId ID турнира
     * @return статус операции
     */
    @PostMapping("/tournaments/{tournamentId}/close")
    public ResponseEntity<String> closeRegistration(@PathVariable Long tournamentId) {
        tournamentService.closeRegistration(tournamentId);
        return ResponseEntity.ok("Registration closed successfully");
    }

    /**
     * Запускает турнир
     * @param tournamentId ID турнира
     * @return статус операции
     */
    @PostMapping("/tournaments/{tournamentId}/start")
    public ResponseEntity<String> startTournament(@PathVariable Long tournamentId) {
        tournamentService.startTournament(tournamentId);
        return ResponseEntity.ok("Tournament started successfully");
    }

    /**
     * Отменяет турнир
     * @param tournamentId ID турнира
     * @return статус операции
     */
    @PostMapping("/tournaments/{tournamentId}/cancel")
    public ResponseEntity<String> cancelTournament(@PathVariable Long tournamentId) {
        tournamentService.cancelTournament(tournamentId);
        return ResponseEntity.ok("Tournament cancelled successfully");
    }

    /**
     * Переходит к следующему раунду
     * @param tournamentId ID турнира
     * @return статус операции
     */
    @PostMapping("/tournaments/{tournamentId}/next-round")
    public ResponseEntity<String> nextRound(@PathVariable Long tournamentId) {
        tournamentService.proceedToNextRound(tournamentId);
        return ResponseEntity.ok("Proceeded to next round successfully");
    }

    /**
     * Регистрирует текущего пользователя на турнир
     * @param tournamentId ID турнира
     * @return статус операции
     */
    @PostMapping("/tournaments/{tournamentId}/register")
    public ResponseEntity<String> registerForTournament(
            @PathVariable Long tournamentId,
            @RequestHeader("Authorization") String authHeader) {
        
        // Получаем токен из заголовка Authorization
        String token = authHeader.substring(7); // Удаляем "Bearer "
        
        // Получаем имя пользователя из токена
        String username = jwtTokenProvider.extractUsername(token);
        
        // Получаем пользователя из базы данных
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        tournamentService.registerPlayer(tournamentId, user);
        return ResponseEntity.ok("Registered for tournament successfully");
    }
    
    /**
     * Регистрирует конкретного пользователя на турнир
     * @param tournamentId ID турнира
     * @param userId ID пользователя
     * @return статус операции
     */
    @PostMapping("/tournaments/{tournamentId}/register/{userId}")
    public ResponseEntity<String> registerUserForTournament(
            @PathVariable Long tournamentId,
            @PathVariable Long userId) {
        // Получаем пользователя по ID
        User userToRegister = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        tournamentService.registerPlayer(tournamentId, userToRegister);
        return ResponseEntity.ok("User registered for tournament successfully");
    }

    /**
     * Отменяет регистрацию текущего пользователя
     * @param tournamentId ID турнира
     * @return статус операции
     */
    @PostMapping("/tournaments/{tournamentId}/unregister")
    public ResponseEntity<String> unregisterFromTournament(
            @PathVariable Long tournamentId,
            @RequestHeader("Authorization") String authHeader) {
        
        // Получаем токен из заголовка Authorization
        String token = authHeader.substring(7); // Удаляем "Bearer "
        
        // Получаем имя пользователя из токена
        String username = jwtTokenProvider.extractUsername(token);
        
        // Получаем пользователя из базы данных
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        tournamentService.unregisterPlayer(tournamentId, user.getId());
        return ResponseEntity.ok("Unregistered from tournament successfully");
    }

    /**
     * Получает список матчей текущего раунда
     * @param tournamentId ID турнира
     * @return список матчей
     */
    @GetMapping("/tournaments/{tournamentId}/matches/current")
    public ResponseEntity<List<MatchResponseDto>> getCurrentRoundMatches(@PathVariable Long tournamentId) {
        List<SwissMatch> matches = tournamentService.getCurrentRoundMatches(tournamentId);
        List<MatchResponseDto> response = matches.stream()
                .map(MatchResponseDto::fromEntity)
                .toList();
        return ResponseEntity.ok(response);
    }

    /**
     * Обновляет результат матча
     * @param tournamentId ID турнира
     * @param dto данные для обновления
     * @return статус операции
     */
    @PostMapping("/tournaments/{tournamentId}/matches/update")
    public ResponseEntity<String> updateMatchResult(
            @PathVariable Long tournamentId,
            @Valid @RequestBody MatchUpdateDto dto) {
        log.info("Received request to update match result for tournament {} match {}", tournamentId, dto.getMatchId());
        try {
            tournamentService.updateMatchResult(tournamentId, dto.getMatchId(), dto.getPlayer1Score(), dto.getPlayer2Score());
            log.info("Match result updated successfully for tournament {} match {}", tournamentId, dto.getMatchId());
            return ResponseEntity.ok("Match result updated successfully");
        } catch (Exception e) {
            // Логируем ошибку, если она дошла до контроллера
            log.error("Error updating match result for tournament {} match {}", tournamentId, dto.getMatchId(), e);
            System.err.println("CONTROLLER ERROR updating match result: " + e.getMessage()); // Дополнительный вывод в stderr
            // Возвращаем 500 ошибку с сообщением
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("Error updating match result: " + e.getMessage());
        }
    }

    /**
     * Получает итоговые результаты турнира
     * @param tournamentId ID турнира
     * @return список результатов
     */
    @GetMapping("/tournaments/{tournamentId}/results")
    public ResponseEntity<List<PlayerResultDto>> getTournamentResults(@PathVariable Long tournamentId) {
        List<PlayerResult> results = tournamentService.getTournamentResults(tournamentId);
        List<PlayerResultDto> response = results.stream()
                .map(PlayerResultDto::fromEntity)
                .toList();
        return ResponseEntity.ok(response);
    }
    
    /**
     * Получает список участников турнира
     * @param tournamentId ID турнира
     * @return список участников
     */
    @GetMapping("/tournaments/{tournamentId}/participants")
    public ResponseEntity<List<ParticipantDto>> getTournamentParticipants(@PathVariable Long tournamentId) {
        List<SwissRegistration> registrations = tournamentService.getTournamentParticipants(tournamentId);
        List<ParticipantDto> response = registrations.stream()
                .map(ParticipantDto::fromEntity)
                .toList();

        return ResponseEntity.ok(response);
    }

    /**
     * Отменяет регистрацию указанного пользователя с турнира
     * @param tournamentId ID турнира
     * @param userId ID пользователя для отмены регистрации
     * @return статус операции
     */
    @PostMapping("/tournaments/{tournamentId}/unregister/{userId}")
    public ResponseEntity<String> unregisterUserFromTournament(
            @PathVariable Long tournamentId,
            @PathVariable Long userId) {

        tournamentService.unregisterPlayer(tournamentId, userId);
        return ResponseEntity.ok("User unregistered from tournament successfully");
    }
}