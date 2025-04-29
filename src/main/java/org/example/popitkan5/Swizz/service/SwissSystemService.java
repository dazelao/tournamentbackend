package org.example.popitkan5.Swizz.service;

import lombok.RequiredArgsConstructor;
import org.example.popitkan5.Swizz.model.*;
import org.example.popitkan5.Swizz.repository.PlayerResultRepository;
import org.example.popitkan5.Swizz.repository.SwissMatchRepository;
import org.example.popitkan5.Swizz.repository.SwissTournamentRepository;
import org.example.popitkan5.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SwissSystemService {
    private final SwissTournamentRepository tournamentRepository;
    private final SwissMatchRepository matchRepository;
    private final PlayerResultRepository playerResultRepository;
    private static final Logger log = LoggerFactory.getLogger(SwissSystemService.class);

    /**
     * Генерирует пары для следующего раунда
     * @param tournament текущий турнир
     * @return список пар для следующего раунда
     */
    @Transactional(readOnly = true)
    public List<SwissPair> generateNextRoundPairs(SwissTournament tournament) {
        // Получаем всех активных игроков
        List<User> players = tournament.getRegistrations().stream()
                .filter(SwissRegistration::isActive)
                .map(SwissRegistration::getUser)
                .toList();

        // Получаем результаты игроков
        List<PlayerResult> results = playerResultRepository.findTournamentResultsSorted(tournament.getId());
        Map<User, PlayerResult> playerResults = new HashMap<>();
        for (PlayerResult result : results) {
            playerResults.put(result.getUser(), result);
        }

        // Сортируем игроков по очкам и разнице мячей (используем уже отсортированные результаты)
        List<User> sortedPlayers = results.stream()
                .map(PlayerResult::getUser)
                .toList();
        
        // Добавляем игроков, которые еще не имеют результатов
        List<User> allSortedPlayers = new ArrayList<>(sortedPlayers);
        players.stream()
                .filter(player -> !playerResults.containsKey(player))
                .forEach(allSortedPlayers::add);

        // Получаем историю матчей для проверки повторных встреч
        Set<Set<Long>> playedMatches = getPlayedMatches(tournament);

        List<SwissPair> pairs = new ArrayList<>();
        List<User> remainingPlayers = new ArrayList<>(allSortedPlayers);

        // Если нечетное количество игроков, даем бай последнему игроку
        if (remainingPlayers.size() % 2 != 0) {
            User byePlayer = remainingPlayers.remove(remainingPlayers.size() - 1);
            pairs.add(new SwissPair(byePlayer, null, true, tournament.getCurrentRound()));
        }

        // Формируем пары
        while (!remainingPlayers.isEmpty()) {
            User player1 = remainingPlayers.remove(0);
            User player2 = findOpponent(player1, remainingPlayers, playedMatches, tournament.getId());
            
            if (player2 != null) {
                remainingPlayers.remove(player2);
                pairs.add(new SwissPair(player1, player2, false, tournament.getCurrentRound()));
            } else {
                // Если не нашли подходящего соперника, даем бай
                pairs.add(new SwissPair(player1, null, true, tournament.getCurrentRound()));
            }
        }

        return pairs;
    }

    /**
     * Получает историю матчей для проверки повторных встреч
     * @param tournament текущий турнир
     * @return множество пар ID игроков, которые уже встречались
     */
    protected Set<Set<Long>> getPlayedMatches(SwissTournament tournament) {
        Set<Set<Long>> playedMatches = new HashSet<>();
        List<SwissMatch> matches = matchRepository.findByTournamentIdAndStatus(tournament.getId(), MatchStatus.COMPLETED);
        
        for (SwissMatch match : matches) {
            if (match.getPlayer2() != null) {
                Set<Long> pair = new HashSet<>();
                pair.add(match.getPlayer1().getId());
                pair.add(match.getPlayer2().getId());
                playedMatches.add(pair);
            }
        }
        
        return playedMatches;
    }

    /**
     * Находит подходящего соперника для игрока
     * @param player игрок, для которого ищем соперника
     * @param candidates список кандидатов
     * @param playedMatches история матчей
     * @param tournamentId ID турнира
     * @return подходящий соперник или null, если не найден
     */
    protected User findOpponent(User player, List<User> candidates, Set<Set<Long>> playedMatches, Long tournamentId) {
        for (User candidate : candidates) {
            Set<Long> potentialMatch = new HashSet<>();
            potentialMatch.add(player.getId());
            potentialMatch.add(candidate.getId());
            
            if (!playedMatches.contains(potentialMatch) && 
                !matchRepository.havePlayersPlayed(tournamentId, player.getId(), candidate.getId())) {
                return candidate;
            }
        }
        return null;
    }

    /**
     * Создает матчи из пар
     * @param tournament текущий турнир
     * @param pairs список пар
     */
    @Transactional
    public void createMatchesFromPairs(SwissTournament tournament, List<SwissPair> pairs) {
        for (SwissPair pair : pairs) {
            SwissMatch match = new SwissMatch();
            match.setTournament(tournament);
            match.setPlayer1(pair.getPlayer1());
            match.setPlayer2(pair.getPlayer2());
            match.setRound(tournament.getCurrentRound());
            match.setStatus(pair.isBye() ? MatchStatus.BYE : MatchStatus.SCHEDULED);
            match.setScheduledTime(LocalDateTime.now());
            
            if (pair.isBye()) {
                // Автоматически засчитываем победу игроку с баем
                match.setPlayer1Score(3);
                match.setPlayer2Score(0);
                match.setStatus(MatchStatus.COMPLETED);
                match.setCompletedTime(LocalDateTime.now());
                
                // Обновляем результаты игрока
                updatePlayerResult(tournament, pair.getPlayer1(), 3, 0);
            }
            
            matchRepository.save(match);
        }
        
        tournamentRepository.save(tournament);
    }

    /**
     * Обновляет результат матча
     * @param tournament текущий турнир
     * @param matchId ID матча
     * @param player1Score счет первого игрока
     * @param player2Score счет второго игрока
     */
    @Transactional
    public void updateMatchResult(SwissTournament tournament, Long matchId, int player1Score, int player2Score) {
        log.info("SYSTEM_SERVICE: Updating result for match {}", matchId);
        try {
            SwissMatch match = matchRepository.findById(matchId)
                    .orElseThrow(() -> {
                        log.error("SYSTEM_SERVICE: Match not found with ID: {}", matchId);
                        return new IllegalArgumentException("Match not found");
                    });
            log.debug("SYSTEM_SERVICE: Found match {}", matchId);

            if (match.getStatus() != MatchStatus.SCHEDULED && match.getStatus() != MatchStatus.IN_PROGRESS) {
                 log.warn("SYSTEM_SERVICE: Match {} has status {}, cannot update.", matchId, match.getStatus());
                 throw new IllegalStateException("Cannot update result of completed or cancelled match");
            }

            match.setPlayer1Score(player1Score);
            match.setPlayer2Score(player2Score);
            match.setStatus(MatchStatus.COMPLETED);
            match.setCompletedTime(LocalDateTime.now());
            matchRepository.save(match);
            log.debug("SYSTEM_SERVICE: Saved updated match {}", matchId);

            User player1 = match.getPlayer1();
            User player2 = match.getPlayer2();

            if (player1 != null && player2 != null) {
                log.debug("SYSTEM_SERVICE: Updating results for players {} and {}", player1.getId(), player2.getId());
                updatePlayerResult(tournament, player1, player1Score, player2Score);
                updatePlayerResult(tournament, player2, player2Score, player1Score);
            } else if (player1 != null) {
                log.debug("SYSTEM_SERVICE: Updating results for player {} (player 2 is null)", player1.getId());
                updatePlayerResult(tournament, player1, player1Score, player2Score);
            } else if (player2 != null) {
                log.debug("SYSTEM_SERVICE: Updating results for player {} (player 1 is null)", player2.getId());
                updatePlayerResult(tournament, player2, player2Score, player1Score);
            } else {
                log.warn("SYSTEM_SERVICE: Both players are null for match {}, skipping player result update.", matchId);
            }
            log.info("SYSTEM_SERVICE: Successfully updated result for match {}", matchId);
        } catch (Exception e) {
            log.error("SYSTEM_SERVICE: Error updating match result for match {}", matchId, e);
            System.err.println("SYSTEM_SERVICE ERROR updating match result: " + e.getMessage()); // Дополнительный вывод в stderr
            throw e; // Перебрасываем
        }
    }

    /**
     * Обновляет результаты игрока
     * @param tournament текущий турнир
     * @param player игрок
     * @param goalsFor забитые голы
     * @param goalsAgainst пропущенные голы
     */
    @Transactional
    protected void updatePlayerResult(SwissTournament tournament, User player, int goalsFor, int goalsAgainst) {
        log.info("SYSTEM_SERVICE: Updating player result for player {} in tournament {}", player.getId(), tournament.getId());
        try {
            PlayerResult result = playerResultRepository.findByTournamentIdAndUserId(tournament.getId(), player.getId())
                    .orElseGet(() -> {
                        log.debug("SYSTEM_SERVICE: Creating new PlayerResult for player {} in tournament {}", player.getId(), tournament.getId());
                        PlayerResult newResult = new PlayerResult();
                        newResult.setTournament(tournament);
                        newResult.setUser(player);
                        // Убедимся, что раунд корректный
                        Integer currentRound = tournament.getCurrentRound();
                        if (currentRound == null) {
                             log.warn("SYSTEM_SERVICE: Tournament currentRound is null for tournament {}! Setting round to 0 for player {}", tournament.getId(), player.getId());
                             currentRound = 0; // Или другое значение по умолчанию?
                        }
                        newResult.setRound(currentRound);
                        newResult.setPoints(0);
                        newResult.setWins(0);
                        newResult.setLosses(0);
                        newResult.setDraws(0);
                        newResult.setGoalsFor(0);
                        newResult.setGoalsAgainst(0);
                        newResult.setLastUpdated(LocalDateTime.now());
                        return newResult;
                    });
            log.debug("SYSTEM_SERVICE: Found or created PlayerResult (ID: {}) for player {}", result.getId(), player.getId());

            result.addMatchResult(goalsFor, goalsAgainst);
            log.debug("SYSTEM_SERVICE: Called addMatchResult for player {}", player.getId());

            playerResultRepository.save(result);
            log.debug("SYSTEM_SERVICE: Saved PlayerResult (ID: {}) for player {}", result.getId(), player.getId());
            log.info("SYSTEM_SERVICE: Successfully updated player result for player {} in tournament {}", player.getId(), tournament.getId());
        } catch (Exception e) {
            log.error("SYSTEM_SERVICE: Error updating player result for player {} in tournament {}", player.getId(), tournament.getId(), e);
            System.err.println("SYSTEM_SERVICE ERROR updating player result: " + e.getMessage()); // Дополнительный вывод в stderr
            throw e; // Перебрасываем
        }
    }

    /**
     * Проверяет, все ли матчи текущего раунда завершены
     * @param tournament текущий турнир
     * @return true, если все матчи завершены
     */
    @Transactional(readOnly = true)
    public boolean isRoundCompleted(SwissTournament tournament) {
        int completedMatches = matchRepository.countCompletedMatchesInRound(tournament.getId(), tournament.getCurrentRound());
        int totalMatches = matchRepository.countTotalMatchesInRound(tournament.getId(), tournament.getCurrentRound());
        
        return completedMatches == totalMatches;
    }

    /**
     * Переходит к следующему раунду
     * @param tournament текущий турнир
     */
    @Transactional
    public void proceedToNextRound(SwissTournament tournament) {
        if (!isRoundCompleted(tournament)) {
            throw new IllegalStateException("Current round is not completed");
        }

        // Проверяем, не является ли текущий раунд последним
        if (tournament.getCurrentRound() >= tournament.getTotalRounds()) {
            tournament.setStatus(SwissTournamentStatus.COMPLETED);
            // Определяем победителя
            determineWinner(tournament);
            tournamentRepository.save(tournament);
            return;
        }

        // Увеличиваем номер текущего раунда
        tournament.setCurrentRound(tournament.getCurrentRound() + 1);
        tournamentRepository.save(tournament);

        // Генерируем пары для следующего раунда
        List<SwissPair> pairs = generateNextRoundPairs(tournament);

        // Создаем матчи
        createMatchesFromPairs(tournament, pairs);
    }

    /**
     * Определяет победителя турнира
     * @param tournament текущий турнир
     */
    @Transactional
    protected void determineWinner(SwissTournament tournament) {
        // Получаем отсортированные результаты игроков
        List<PlayerResult> sortedResults = playerResultRepository.findTournamentResultsSorted(tournament.getId());
        
        // Устанавливаем места игроков
        for (int i = 0; i < sortedResults.size(); i++) {
            PlayerResult result = sortedResults.get(i);
            result.setPlace(i + 1);
            playerResultRepository.save(result);
        }
        
        // Устанавливаем победителя
        if (!sortedResults.isEmpty()) {
            tournament.setWinner(sortedResults.get(0).getUser());
        }
    }
}
