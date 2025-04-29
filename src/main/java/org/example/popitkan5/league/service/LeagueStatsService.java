package org.example.popitkan5.league.service;

import org.example.popitkan5.league.dto.LeagueStandingsResponse;
import org.example.popitkan5.league.exception.LeagueException;
import org.example.popitkan5.league.model.League;
import org.example.popitkan5.league.model.LeagueHistory;
import org.example.popitkan5.league.model.LeagueMatch;
import org.example.popitkan5.league.model.LeagueStatus;
import org.example.popitkan5.league.repository.LeagueHistoryRepository;
import org.example.popitkan5.league.repository.LeagueMatchRepository;
import org.example.popitkan5.league.repository.LeagueRepository;
import org.example.popitkan5.model.User;
import org.example.popitkan5.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class LeagueStatsService {

    private final LeagueHistoryRepository historyRepository;
    private final LeagueMatchRepository matchRepository;
    private final LeagueRepository leagueRepository;
    private final UserRepository userRepository;

    @Autowired
    public LeagueStatsService(LeagueHistoryRepository historyRepository,
                             LeagueMatchRepository matchRepository,
                             LeagueRepository leagueRepository,
                             UserRepository userRepository) {
        this.historyRepository = historyRepository;
        this.matchRepository = matchRepository;
        this.leagueRepository = leagueRepository;
        this.userRepository = userRepository;
    }

    /**
     * Инициализирует запись в таблице истории лиги для игрока
     */
    @Transactional
    public void initializeLeagueHistory(Long leagueId, Long userId, int totalParticipants) {
        // Проверяем, есть ли уже запись
        Optional<LeagueHistory> existingHistory = historyRepository.findByLeagueIdAndUserId(leagueId, userId);
        if (existingHistory.isPresent()) {
            return; // Запись уже существует
        }

        LeagueHistory history = new LeagueHistory();
        history.setLeagueId(leagueId);
        history.setUserId(userId);
        history.setMatchesPlayed(0);
        history.setWins(0);
        history.setLosses(0);
        history.setDraws(0);
        history.setGoalsScored(0);
        history.setGoalsConceded(0);
        history.setPoints(0);
        history.setPosition(totalParticipants); // По умолчанию - последнее место

        historyRepository.save(history);
    }
    /**
     * Обновляет статистику после завершения матча
     */
   /* @Transactional
    public void updateStats(Long matchId) {
        // Получаем информацию о матче
        LeagueMatch match = matchRepository.findById(matchId)
                .orElseThrow(() -> new LeagueException("Матч с ID " + matchId + " не найден"));

        // Если результат матча еще не установлен, не обновляем статистику
        if (match.getGoalsUser1() == null || match.getGoalsUser2() == null) {
            return;
        }

        Long userId1 = match.getUserId1();
        Long userId2 = match.getUserId2();
        Long leagueId = match.getLeagueId();

        // Проверяем, что лига активна
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new LeagueException("Лига не найдена"));
        if (league.getStatus() != LeagueStatus.ACTIVE) {
            throw new LeagueException("Обновление статистики возможно только для активных лиг");
        }

        // Получаем записи истории для обоих игроков
        LeagueHistory history1 = historyRepository.findByLeagueIdAndUserId(leagueId, userId1)
                .orElseThrow(() -> new LeagueException("Запись истории не найдена для игрока " + userId1));
        LeagueHistory history2 = historyRepository.findByLeagueIdAndUserId(leagueId, userId2)
                .orElseThrow(() -> new LeagueException("Запись истории не найдена для игрока " + userId2));

        // Обновляем количество матчей
        history1.setMatchesPlayed(history1.getMatchesPlayed() + 1);
        history2.setMatchesPlayed(history2.getMatchesPlayed() + 1);

        // Обновляем голы
        history1.setGoalsScored(history1.getGoalsScored() + match.getGoalsUser1());
        history1.setGoalsConceded(history1.getGoalsConceded() + match.getGoalsUser2());
        history2.setGoalsScored(history2.getGoalsScored() + match.getGoalsUser2());
        history2.setGoalsConceded(history2.getGoalsConceded() + match.getGoalsUser1());

        // Обновляем победы, поражения, ничьи и очки
        if (match.getIsDraw()) {
            // Ничья
            history1.setDraws(history1.getDraws() + 1);
            history2.setDraws(history2.getDraws() + 1);
            history1.setPoints(history1.getPoints() + 1);
            history2.setPoints(history2.getPoints() + 1);
        } else if (match.getWinnerId().equals(userId1)) {
            // Победа игрока 1
            history1.setWins(history1.getWins() + 1);
            history2.setLosses(history2.getLosses() + 1);
            history1.setPoints(history1.getPoints() + 3);
        } else {
            // Победа игрока 2
            history2.setWins(history2.getWins() + 1);
            history1.setLosses(history1.getLosses() + 1);
            history2.setPoints(history2.getPoints() + 3);
        }

        // Сохраняем обновленную статистику
        historyRepository.save(history1);
        historyRepository.save(history2);

        // Пересчитываем позиции игроков в таблице
        recalculatePositions(leagueId);
    }*/

    @Transactional
    public void updateStats(Long matchId) {
        // Получаем информацию о матче
        LeagueMatch match = matchRepository.findById(matchId)
                .orElseThrow(() -> new LeagueException("Матч с ID " + matchId + " не найден"));

        // Если результат матча еще не установлен, не обновляем статистику
        if (match.getGoalsUser1() == null || match.getGoalsUser2() == null) {
            return;
        }

        Long leagueId = match.getLeagueId();

        // Проверяем, что лига активна
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new LeagueException("Лига не найдена"));
        if (league.getStatus() != LeagueStatus.ACTIVE) {
            throw new LeagueException("Обновление статистики возможно только для активных лиг");
        }

        // Если это обновление существующего результата, пересчитываем всю статистику
        if (match.getWinnerId() != null) {
            recalculateAllStats(leagueId);
        } else {
            // Если это новый результат, обновляем только для двух игроков
            updateStatsForMatch(match);
        }

        // Пересчитываем позиции игроков в таблице
        recalculatePositions(leagueId);
    }

    private void recalculateAllStats(Long leagueId) {
        // Получаем все записи истории для лиги
        List<LeagueHistory> allHistories = historyRepository.findByLeagueIdOrderByPositionAsc(leagueId);

        // Сбрасываем статистику для всех игроков
        for (LeagueHistory history : allHistories) {
            history.setMatchesPlayed(0);
            history.setWins(0);
            history.setDraws(0);
            history.setLosses(0);
            history.setGoalsScored(0);
            history.setGoalsConceded(0);
            history.setPoints(0);
            historyRepository.save(history);
        }

        // Получаем все сыгранные матчи лиги
        List<LeagueMatch> allMatches = matchRepository.findByLeagueId(leagueId);

        // Пересчитываем статистику для каждого матча
        for (LeagueMatch match : allMatches) {
            if (match.getGoalsUser1() != null && match.getGoalsUser2() != null) {
                updateStatsForMatch(match);
            }
        }
    }

    private void updateStatsForMatch(LeagueMatch match) {
        Long userId1 = match.getUserId1();
        Long userId2 = match.getUserId2();
        Long leagueId = match.getLeagueId();

        LeagueHistory history1 = historyRepository.findByLeagueIdAndUserId(leagueId, userId1)
                .orElseThrow(() -> new LeagueException("Запись истории не найдена для игрока " + userId1));
        LeagueHistory history2 = historyRepository.findByLeagueIdAndUserId(leagueId, userId2)
                .orElseThrow(() -> new LeagueException("Запись истории не найдена для игрока " + userId2));

        // Обновляем количество матчей
        history1.setMatchesPlayed(history1.getMatchesPlayed() + 1);
        history2.setMatchesPlayed(history2.getMatchesPlayed() + 1);

        // Обновляем голы
        history1.setGoalsScored(history1.getGoalsScored() + match.getGoalsUser1());
        history1.setGoalsConceded(history1.getGoalsConceded() + match.getGoalsUser2());
        history2.setGoalsScored(history2.getGoalsScored() + match.getGoalsUser2());
        history2.setGoalsConceded(history2.getGoalsConceded() + match.getGoalsUser1());

        // Обновляем победы, поражения, ничьи и очки
        if (match.getIsDraw()) {
            // Ничья
            history1.setDraws(history1.getDraws() + 1);
            history2.setDraws(history2.getDraws() + 1);
            history1.setPoints(history1.getPoints() + 1);
            history2.setPoints(history2.getPoints() + 1);
        } else if (match.getWinnerId().equals(userId1)) {
            // Победа игрока 1
            history1.setWins(history1.getWins() + 1);
            history2.setLosses(history2.getLosses() + 1);
            history1.setPoints(history1.getPoints() + 3);
        } else {
            // Победа игрока 2
            history2.setWins(history2.getWins() + 1);
            history1.setLosses(history1.getLosses() + 1);
            history2.setPoints(history2.getPoints() + 3);
        }

        // Сохраняем обновленную статистику
        historyRepository.save(history1);
        historyRepository.save(history2);
    }
    /**
     * Пересчитывает позиции игроков в таблице лиги
     */
    @Transactional
    public void recalculatePositions(Long leagueId) {
        // Получаем все записи истории для лиги, отсортированные по очкам и разнице голов
        List<LeagueHistory> historyList = historyRepository.findByLeagueIdOrderByPointsAndGoalDifference(leagueId);

        // Группируем записи по очкам и разнице голов для обработки ничьих
        Map<String, List<LeagueHistory>> groupedByPoints = new HashMap<>();
        for (LeagueHistory history : historyList) {
            String key = history.getPoints() + "_" + (history.getGoalsScored() - history.getGoalsConceded());
            groupedByPoints.computeIfAbsent(key, k -> new ArrayList<>()).add(history);
        }

        // Обрабатываем группы с одинаковыми очками и разницей голов
        int position = 1;
        for (int i = 0; i < historyList.size(); i++) {
            LeagueHistory history = historyList.get(i);
            String key = history.getPoints() + "_" + (history.getGoalsScored() - history.getGoalsConceded());
            List<LeagueHistory> samePointsGroup = groupedByPoints.get(key);

            // Если группа уже обработана, пропускаем
            if (samePointsGroup == null || samePointsGroup.isEmpty()) {
                continue;
            }

            // Если в группе более одного игрока, проверяем личные встречи
            if (samePointsGroup.size() > 1) {
                resolveByHeadToHead(samePointsGroup, leagueId, position);
            } else {
                // Если только один игрок, просто устанавливаем позицию
                samePointsGroup.get(0).setPosition(position);
                historyRepository.save(samePointsGroup.get(0));
            }

            // Увеличиваем позицию на количество обработанных игроков
            position += samePointsGroup.size();
            // Очищаем группу, чтобы не обрабатывать повторно
            groupedByPoints.put(key, new ArrayList<>());
        }
    }

    /**
     * Разрешает ничьи по личным встречам
     */
    private void resolveByHeadToHead(List<LeagueHistory> samePointsGroup, Long leagueId, int startPosition) {
        // Если только один игрок, просто устанавливаем позицию
        if (samePointsGroup.size() == 1) {
            samePointsGroup.get(0).setPosition(startPosition);
            historyRepository.save(samePointsGroup.get(0));
            return;
        }

        // Создаем таблицу очков для личных встреч
        Map<Long, Integer> headToHeadPoints = new HashMap<>();
        Map<Long, Integer> headToHeadGoalDiff = new HashMap<>();

        // Инициализируем счетчики
        for (LeagueHistory history : samePointsGroup) {
            headToHeadPoints.put(history.getUserId(), 0);
            headToHeadGoalDiff.put(history.getUserId(), 0);
        }

        // Собираем ID всех игроков в группе
        List<Long> userIds = samePointsGroup.stream()
                .map(LeagueHistory::getUserId)
                .collect(Collectors.toList());

        // Получаем все личные встречи между игроками в группе
        for (int i = 0; i < userIds.size(); i++) {
            for (int j = i + 1; j < userIds.size(); j++) {
                Long userId1 = userIds.get(i);
                Long userId2 = userIds.get(j);

                List<LeagueMatch> personalMatches = matchRepository.findPersonalMatches(leagueId, userId1, userId2);

                // Обрабатываем результаты каждого матча
                for (LeagueMatch match : personalMatches) {
                    // Пропускаем незавершенные матчи
                    if (match.getGoalsUser1() == null || match.getGoalsUser2() == null) {
                        continue;
                    }

                    Long player1 = match.getUserId1();
                    Long player2 = match.getUserId2();

                    // Обновляем разницу голов
                    int goalDiff1 = match.getGoalsUser1() - match.getGoalsUser2();
                    headToHeadGoalDiff.put(player1, headToHeadGoalDiff.get(player1) + goalDiff1);
                    headToHeadGoalDiff.put(player2, headToHeadGoalDiff.get(player2) - goalDiff1);

                    // Обновляем очки
                    if (match.getIsDraw()) {
                        // Ничья
                        headToHeadPoints.put(player1, headToHeadPoints.get(player1) + 1);
                        headToHeadPoints.put(player2, headToHeadPoints.get(player2) + 1);
                    } else if (match.getWinnerId().equals(player1)) {
                        // Победа игрока 1
                        headToHeadPoints.put(player1, headToHeadPoints.get(player1) + 3);
                    } else {
                        // Победа игрока 2
                        headToHeadPoints.put(player2, headToHeadPoints.get(player2) + 3);
                    }
                }
            }
        }

        // Сортируем игроков по очкам в личных встречах и разнице голов
        samePointsGroup.sort((h1, h2) -> {
            int pointsDiff = headToHeadPoints.get(h2.getUserId()) - headToHeadPoints.get(h1.getUserId());
            if (pointsDiff != 0) {
                return pointsDiff;
            }
            return headToHeadGoalDiff.get(h2.getUserId()) - headToHeadGoalDiff.get(h1.getUserId());
        });

        // Устанавливаем позиции
        int currentPos = startPosition;
        int samePosition = 0;
        int samePositionCount = 1;

        for (int i = 0; i < samePointsGroup.size(); i++) {
            LeagueHistory history = samePointsGroup.get(i);

            // Если это первый элемент или он имеет другие показатели, чем предыдущий
            if (i == 0) {
                samePosition = currentPos;
            } else {
                LeagueHistory prevHistory = samePointsGroup.get(i - 1);
                if (headToHeadPoints.get(history.getUserId()).equals(headToHeadPoints.get(prevHistory.getUserId())) &&
                    headToHeadGoalDiff.get(history.getUserId()).equals(headToHeadGoalDiff.get(prevHistory.getUserId()))) {
                    // Полностью одинаковые показатели, даем ту же позицию
                    samePositionCount++;
                } else {
                    samePosition = currentPos;
                    samePositionCount = 1;
                }
            }

            // Устанавливаем позицию
            history.setPosition(samePosition);
            historyRepository.save(history);

            currentPos++;
        }
    }

    /**
     * Получает таблицу лиги
     */
    public LeagueStandingsResponse getLeagueStandings(Long leagueId) {
        // Проверяем существование лиги
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new LeagueException("Лига с ID " + leagueId + " не найдена"));
        
        // Получаем записи истории, отсортированные по позиции
        List<LeagueHistory> historyList = historyRepository.findByLeagueIdOrderByPositionAsc(leagueId);
        
        // Загружаем информацию о пользователях
        Set<Long> userIds = historyList.stream()
                .map(LeagueHistory::getUserId)
                .collect(Collectors.toSet());
        
        Map<Long, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, user -> user));
        
        // Формируем ответ
        List<LeagueStandingsResponse.StandingEntry> standings = new ArrayList<>();
        
        for (LeagueHistory history : historyList) {
            User user = userMap.getOrDefault(history.getUserId(), new User());
            
            standings.add(LeagueStandingsResponse.StandingEntry.builder()
                    .userId(history.getUserId())
                    .username(user.getUsername())
                    .position(history.getPosition())
                    .points(history.getPoints())
                    .matchesPlayed(history.getMatchesPlayed())
                    .wins(history.getWins())
                    .draws(history.getDraws())
                    .losses(history.getLosses())
                    .goalsScored(history.getGoalsScored())
                    .goalsConceded(history.getGoalsConceded())
                    .goalDifference(history.getGoalsScored() - history.getGoalsConceded())
                    .build());
        }
        
        return LeagueStandingsResponse.builder()
                .leagueId(leagueId)
                .leagueName(league.getName())
                .standings(standings)
                .build();
    }

    /**
     * Распределяет атрибуты после завершения лиги
     */
    @Transactional
    public void distributeAttributes(Long leagueId) {
        // Проверяем существование лиги
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new LeagueException("Лига с ID " + leagueId + " не найдена"));
        
        if (league.getStatus() != LeagueStatus.FINISHED) {
            throw new LeagueException("Распределение атрибутов возможно только для завершенных лиг");
        }
        
        // Проверяем наличие атрибутов для распределения
        if (league.getWinnerAttribute() == null && league.getSaveAttribute() == null && league.getLoserAttribute() == null) {
            return; // Нет атрибутов для распределения
        }
        
        // Получаем записи истории, отсортированные по позиции
        List<LeagueHistory> historyList = historyRepository.findByLeagueIdOrderByPositionAsc(leagueId);
        
        // Загружаем информацию о пользователях
        Set<Long> userIds = historyList.stream()
                .map(LeagueHistory::getUserId)
                .collect(Collectors.toSet());
        
        List<User> users = userRepository.findAllById(userIds);
        Map<Long, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, user -> user));
        
        // Распределяем атрибуты согласно позициям
        int totalParticipants = historyList.size();
        
        // Определяем количество пользователей для каждого атрибута
        int winnerCount = league.getWinnerCount() != null ? league.getWinnerCount() : 0;
        int loserCount = league.getLoserCount() != null ? league.getLoserCount() : 0;
        int saveCount;
        
        // Если указаны только атрибуты для победителей и сохранивших прописку
        if (league.getLoserAttribute() == null && league.getWinnerAttribute() != null && league.getSaveAttribute() != null) {
            saveCount = totalParticipants - winnerCount;
        } else {
            saveCount = league.getSaveCount() != null ? league.getSaveCount() : 0;
        }
        
        // Расставляем атрибуты пользователям
        for (int i = 0; i < historyList.size(); i++) {
            LeagueHistory history = historyList.get(i);
            User user = userMap.get(history.getUserId());
            
            if (user == null) {
                continue; // Пользователь не найден
            }
            
            // Добавляем атрибут в зависимости от позиции
            if (i < winnerCount && league.getWinnerAttribute() != null) {
                // Победитель
                user.getAttributes().put(league.getWinnerAttribute(), "true");
            } else if (i >= totalParticipants - loserCount && league.getLoserAttribute() != null) {
                // Проигравший
                user.getAttributes().put(league.getLoserAttribute(), "true");
            } else if (i >= winnerCount && i < totalParticipants - loserCount && league.getSaveAttribute() != null) {
                // Сохранивший прописку
                user.getAttributes().put(league.getSaveAttribute(), "true");
            }
            
            userRepository.save(user);
        }
    }
}
