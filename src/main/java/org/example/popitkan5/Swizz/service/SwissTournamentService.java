package org.example.popitkan5.Swizz.service;

import lombok.RequiredArgsConstructor;
import org.example.popitkan5.Swizz.dto.TournamentCreateDto;
import org.example.popitkan5.Swizz.model.*;
import org.example.popitkan5.Swizz.repository.PlayerResultRepository;
import org.example.popitkan5.Swizz.repository.SwissMatchRepository;
import org.example.popitkan5.Swizz.repository.SwissRegistrationRepository;
import org.example.popitkan5.Swizz.repository.SwissTournamentRepository;
import org.example.popitkan5.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SwissTournamentService {
    private final SwissTournamentRepository tournamentRepository;
    private final SwissRegistrationRepository registrationRepository;
    private final SwissMatchRepository matchRepository;
    private final PlayerResultRepository playerResultRepository;
    private final FirstRoundService firstRoundService;
    private final SwissSystemService swissSystemService;
    private static final Logger log = LoggerFactory.getLogger(SwissTournamentService.class);

    /**
     * Создает новый турнир
     * @param dto данные для создания турнира
     * @return созданный турнир
     */
    @Transactional
    public SwissTournament createTournament(TournamentCreateDto dto) {
        SwissTournament tournament = new SwissTournament();
        tournament.setName(dto.getName());
        tournament.setDescription(dto.getDescription());
        tournament.setMaxPlayers(dto.getMaxPlayers());
        tournament.setStartDate(dto.getStartDate());
        tournament.setEndDate(dto.getEndDate());
        tournament.setStatus(SwissTournamentStatus.CREATED);
        tournament.setCurrentRound(0);
        tournament.setTotalRounds(0);
        return tournamentRepository.save(tournament);
    }

    /**
     * Получает турнир по ID
     * @param tournamentId ID турнира
     * @return турнир
     */
    @Transactional(readOnly = true)
    public SwissTournament getTournamentById(Long tournamentId) {
        return tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new IllegalArgumentException("Tournament not found"));
    }

    /**
     * Получает список всех турниров
     * @return список турниров
     */
    @Transactional(readOnly = true)
    public List<SwissTournament> getAllTournaments() {
        return tournamentRepository.findAll();
    }

    /**
     * Получает список активных турниров
     * @return список активных турниров
     */
    @Transactional(readOnly = true)
    public List<SwissTournament> getActiveTournaments() {
        return tournamentRepository.findActiveTournaments();
    }

    /**
     * Получает список турниров с открытой регистрацией
     * @return список турниров с открытой регистрацией
     */
    @Transactional(readOnly = true)
    public List<SwissTournament> getOpenRegistrationTournaments() {
        return tournamentRepository.findActiveRegistrations();
    }

    /**
     * Открывает регистрацию на турнир
     * @param tournamentId ID турнира
     */
    @Transactional
    public void openRegistration(Long tournamentId) {
        SwissTournament tournament = getTournamentById(tournamentId);
        if (tournament.getStatus() != SwissTournamentStatus.CREATED) {
            throw new IllegalStateException("Tournament is not in CREATED state");
        }
        
        tournament.setStatus(SwissTournamentStatus.REGISTRATION_OPEN);
        tournamentRepository.save(tournament);
    }

    /**
     * Регистрирует игрока на турнир
     * @param tournamentId ID турнира
     * @param user игрок
     */
    @Transactional
    public void registerPlayer(Long tournamentId, User user) {
        SwissTournament tournament = getTournamentById(tournamentId);
        
        // Проверяем, открыта ли регистрация
        if (tournament.getStatus() != SwissTournamentStatus.REGISTRATION_OPEN) {
            throw new IllegalStateException("Registration is not open for this tournament");
        }
        
        // Проверяем, не достигнуто ли максимальное количество игроков
        int activeRegistrations = registrationRepository.countActiveRegistrations(tournamentId);
        
        if (activeRegistrations >= tournament.getMaxPlayers()) {
            throw new IllegalStateException("Maximum number of players reached");
        }
        
        // Проверяем, не зарегистрирован ли игрок уже
        if (registrationRepository.findActiveRegistration(tournamentId, user.getId()).isPresent()) {
            throw new IllegalStateException("Player is already registered");
        }
        
        // Регистрируем игрока
        SwissRegistration registration = new SwissRegistration(tournament, user);
        registrationRepository.save(registration);
    }

    /**
     * Отменяет регистрацию игрока
     * @param tournamentId ID турнира
     * @param userId ID игрока
     */
    @Transactional
    public void unregisterPlayer(Long tournamentId, Long userId) {
        SwissTournament tournament = getTournamentById(tournamentId);
        
        // Проверяем, открыта ли регистрация
        if (tournament.getStatus() != SwissTournamentStatus.REGISTRATION_OPEN) {
            throw new IllegalStateException("Registration is not open for this tournament");
        }
        
        // Находим регистрацию игрока
        SwissRegistration registration = registrationRepository.findActiveRegistration(tournamentId, userId)
                .orElseThrow(() -> new IllegalStateException("Player is not registered"));
        
        // Деактивируем регистрацию
        registration.setActive(false);
        registrationRepository.save(registration);
    }

    /**
     * Закрывает регистрацию на турнир
     * @param tournamentId ID турнира
     */
    @Transactional
    public void closeRegistration(Long tournamentId) {
        SwissTournament tournament = getTournamentById(tournamentId);
        
        if (tournament.getStatus() != SwissTournamentStatus.REGISTRATION_OPEN) {
            throw new IllegalStateException("Registration is not open for this tournament");
        }
        
        tournament.setStatus(SwissTournamentStatus.REGISTRATION_CLOSED);
        tournamentRepository.save(tournament);
    }

    /**
     * Запускает турнир
     * @param tournamentId ID турнира
     */
    @Transactional
    public void startTournament(Long tournamentId) {
        SwissTournament tournament = getTournamentById(tournamentId);
        
        if (tournament.getStatus() != SwissTournamentStatus.REGISTRATION_CLOSED) {
            throw new IllegalStateException("Tournament is not ready to start");
        }
        
        // Проверяем, есть ли достаточное количество игроков
        int activeRegistrations = registrationRepository.countActiveRegistrations(tournamentId);
        
        if (activeRegistrations < 2) {
            throw new IllegalStateException("Not enough players to start tournament");
        }
        
        // Инициализируем турнир
        firstRoundService.initializeTournament(tournament);
    }

    /**
     * Обновляет результат матча
     * @param tournamentId ID турнира
     * @param matchId ID матча
     * @param player1Score счет первого игрока
     * @param player2Score счет второго игрока
     */
    @Transactional
    public void updateMatchResult(Long tournamentId, Long matchId, int player1Score, int player2Score) {
        log.info("SERVICE: Attempting to update result for match {} in tournament {}", matchId, tournamentId);
        try {
            SwissTournament tournament = getTournamentById(tournamentId);
            log.debug("SERVICE: Found tournament {}", tournamentId);

            if (tournament.getStatus() != SwissTournamentStatus.IN_PROGRESS) {
                log.warn("SERVICE: Tournament {} is not in progress (status: {}), cannot update match {}", tournamentId, tournament.getStatus(), matchId);
                throw new IllegalStateException("Tournament is not in progress");
            }

            swissSystemService.updateMatchResult(tournament, matchId, player1Score, player2Score);
            log.debug("SERVICE: Called swissSystemService.updateMatchResult for match {}", matchId);

            // Проверяем, не завершен ли раунд
            if (swissSystemService.isRoundCompleted(tournament)) {
                log.info("SERVICE: Round {} completed for tournament {}", tournament.getCurrentRound(), tournamentId);
                // Проверяем, не завершен ли турнир
                if (tournament.getCurrentRound() >= tournament.getTotalRounds()) {
                    log.info("SERVICE: Tournament {} finished, completing...", tournamentId);
                    completeTournament(tournament);
                }
            }
            log.info("SERVICE: Successfully updated result for match {} in tournament {}", matchId, tournamentId);
        } catch (Exception e) {
            log.error("SERVICE: Error updating match result for match {} in tournament {}", matchId, tournamentId, e);
            System.err.println("SERVICE ERROR updating match result: " + e.getMessage()); // Дополнительный вывод в stderr
            throw e; // Перебрасываем исключение, чтобы транзакция откатилась и контроллер вернул 500
        }
    }

    /**
     * Переходит к следующему раунду
     * @param tournamentId ID турнира
     */
    @Transactional
    public void proceedToNextRound(Long tournamentId) {
        SwissTournament tournament = getTournamentById(tournamentId);
        
        if (tournament.getStatus() != SwissTournamentStatus.IN_PROGRESS) {
            throw new IllegalStateException("Tournament is not in progress");
        }
        
        swissSystemService.proceedToNextRound(tournament);
    }

    /**
     * Завершает турнир
     * @param tournament турнир
     */
    @Transactional
    public void completeTournament(SwissTournament tournament) {
        if (tournament.getStatus() != SwissTournamentStatus.IN_PROGRESS) {
            throw new IllegalStateException("Tournament is not in progress");
        }
        
        if (!swissSystemService.isRoundCompleted(tournament)) {
            throw new IllegalStateException("Current round is not completed");
        }
        
        tournament.setStatus(SwissTournamentStatus.COMPLETED);
        tournamentRepository.save(tournament);
        
        // Определяем победителя и места игроков
        swissSystemService.proceedToNextRound(tournament);
    }

    /**
     * Отменяет турнир
     * @param tournamentId ID турнира
     */
    @Transactional
    public void cancelTournament(Long tournamentId) {
        SwissTournament tournament = getTournamentById(tournamentId);
        tournament.setStatus(SwissTournamentStatus.CANCELLED);
        tournamentRepository.save(tournament);
    }

    /**
     * Получает список матчей текущего раунда
     * @param tournamentId ID турнира
     * @return список матчей
     */
    @Transactional(readOnly = true)
    public List<SwissMatch> getCurrentRoundMatches(Long tournamentId) {
        SwissTournament tournament = getTournamentById(tournamentId);
        return matchRepository.findByTournamentIdAndRound(tournamentId, tournament.getCurrentRound());
    }

    /**
     * Получает список участников турнира
     * @param tournamentId ID турнира
     * @return список регистраций активных участников
     */
    @Transactional(readOnly = true)
    public List<SwissRegistration> getTournamentParticipants(Long tournamentId) {
        // Проверяем, существует ли турнир
        getTournamentById(tournamentId);
        
        // Возвращаем только активных участников
        return registrationRepository.findByTournamentIdAndActiveTrue(tournamentId);
    }
    
    /**
     * Получает итоговые результаты турнира
     * @param tournamentId ID турнира
     * @return список результатов игроков, отсортированный по местам
     */
    @Transactional(readOnly = true)
    public List<PlayerResult> getTournamentResults(Long tournamentId) {
        SwissTournament tournament = getTournamentById(tournamentId);

        // Убираем проверку на статус FINISHED/COMPLETED, чтобы возвращать текущие результаты
        /*
        if (tournament.getStatus() != SwissTournamentStatus.COMPLETED) { // Или FINISHED, в зависимости от вашей логики
            throw new IllegalStateException("Tournament is not completed");
        }
        */

        // Возвращаем результаты, отсортированные по очкам и Бухгольцу (используя предполагаемый существующий метод)
        return playerResultRepository.findTournamentResultsSorted(tournamentId);
    }
}
