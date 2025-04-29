package org.example.popitkan5.Swizz.service;

import lombok.RequiredArgsConstructor;
import org.example.popitkan5.Swizz.model.*;
import org.example.popitkan5.Swizz.repository.SwissRegistrationRepository;
import org.example.popitkan5.Swizz.repository.SwissTournamentRepository;
import org.example.popitkan5.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FirstRoundService {
    private final SwissTournamentRepository tournamentRepository;
    private final SwissRegistrationRepository registrationRepository;
    private final SwissSystemService swissSystemService;

    /**
     * Рассчитывает количество раундов по формуле R = ⌈log₂(N)⌉
     * @param numberOfPlayers количество игроков
     * @return количество раундов
     */
    public int calculateNumberOfRounds(int numberOfPlayers) {
        return (int) Math.ceil(Math.log(numberOfPlayers) / Math.log(2));
    }

    /**
     * Формирует пары для первого раунда
     * @param tournament турнир
     * @return список пар
     */
    @Transactional(readOnly = true)
    public List<SwissPair> generateFirstRoundPairs(SwissTournament tournament) {
        // Получаем всех зарегистрированных игроков
        List<SwissRegistration> registrations = registrationRepository.findByTournamentIdAndActiveTrue(tournament.getId());
        List<User> players = registrations.stream()
                .map(SwissRegistration::getUser)
                .toList();

        // Перемешиваем игроков для случайного распределения
        List<User> shuffledPlayers = new ArrayList<>(players);
        Collections.shuffle(shuffledPlayers);

        List<SwissPair> pairs = new ArrayList<>();
        List<User> remainingPlayers = new ArrayList<>(shuffledPlayers);

        // Если нечетное количество игроков, даем бай случайному игроку
        if (remainingPlayers.size() % 2 != 0) {
            int randomIndex = new Random().nextInt(remainingPlayers.size());
            User byePlayer = remainingPlayers.remove(randomIndex);
            pairs.add(new SwissPair(byePlayer, null, true, 1));
        }

        // Формируем пары
        while (!remainingPlayers.isEmpty()) {
            User player1 = remainingPlayers.remove(0);
            if (!remainingPlayers.isEmpty()) {
                User player2 = remainingPlayers.remove(0);
                pairs.add(new SwissPair(player1, player2, false, 1));
            } else {
                // Если остался один игрок, даем ему бай
                pairs.add(new SwissPair(player1, null, true, 1));
            }
        }

        return pairs;
    }

    /**
     * Инициализирует турнир по Швейцарской системе
     * @param tournament турнир
     */
    @Transactional
    public void initializeTournament(SwissTournament tournament) {
        // Проверяем, что турнир в правильном статусе
        if (tournament.getStatus() != SwissTournamentStatus.REGISTRATION_CLOSED) {
            throw new IllegalStateException("Tournament is not ready to start");
        }

        // Рассчитываем количество раундов
        int numberOfPlayers = registrationRepository.countActiveRegistrations(tournament.getId());
        int numberOfRounds = calculateNumberOfRounds(numberOfPlayers);
        tournament.setTotalRounds(numberOfRounds);
        tournament.setCurrentRound(1);

        // Формируем пары для первого раунда
        List<SwissPair> pairs = generateFirstRoundPairs(tournament);

        // Обновляем статус турнира
        tournament.setStatus(SwissTournamentStatus.IN_PROGRESS);
        tournamentRepository.save(tournament);

        // Создаем матчи
        swissSystemService.createMatchesFromPairs(tournament, pairs);
    }

    /**
     * Проверяет, завершен ли турнир
     * @param tournament турнир
     * @return true, если турнир завершен
     */
    @Transactional(readOnly = true)
    public boolean isTournamentCompleted(SwissTournament tournament) {
        return tournament.getCurrentRound() > tournament.getTotalRounds() &&
               swissSystemService.isRoundCompleted(tournament);
    }
}
