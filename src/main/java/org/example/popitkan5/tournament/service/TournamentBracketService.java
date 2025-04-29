package org.example.popitkan5.tournament.service;

import org.example.popitkan5.games.dto.BulkCreateMatchesRequest;
import org.example.popitkan5.games.dto.MatchResultRequest;
import org.example.popitkan5.games.dto.MatchResponse;
import org.example.popitkan5.games.service.MatchService;
import org.example.popitkan5.tournament.exception.TournamentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TournamentBracketService {

    private final MatchService matchService;

    @Autowired
    public TournamentBracketService(MatchService matchService) {
        this.matchService = matchService;
    }

    /**
     * Создает сетку первого раунда турнира
     * 
     * @param tournamentId ID турнира
     * @param participantIds Список ID участников (уже перемешанных)
     * @return Количество созданных матчей
     */
    @Transactional
    public int createFirstRound(Long tournamentId, List<Long> participantIds) {
        if (participantIds.isEmpty()) {
            throw new TournamentException("Список участников пуст");
        }

        int participantsCount = participantIds.size();
        
        // Вычисляем общее количество требуемых раундов
        int totalRounds = (int) Math.ceil(Math.log(participantsCount) / Math.log(2));
        
        // Вычисляем идеальный размер сетки (ближайшая степень 2)
        int perfectBracketSize = (int) Math.pow(2, totalRounds);
        
        // Вычисляем количество нужных баев (проходов без игры)
        int requiredByes = perfectBracketSize - participantsCount;
        
        // Количество матчей первого раунда (с реальными игроками)
        int firstRoundRealMatches = participantsCount - requiredByes;
        
        // Создаем список матчей с реальными игроками
        List<BulkCreateMatchesRequest.MatchData> matches = new ArrayList<>();
        
        // Добавляем матчи с реальными игроками
        for (int i = 0; i < firstRoundRealMatches; i += 2) {
            BulkCreateMatchesRequest.MatchData matchData = new BulkCreateMatchesRequest.MatchData(
                    participantIds.get(i), 
                    participantIds.get(i + 1));
            matches.add(matchData);
        }
        
        // Создаем запрос для массового создания матчей
        BulkCreateMatchesRequest request = BulkCreateMatchesRequest.builder()
                .tournamentId(tournamentId)
                .roundNumber(1)
                .matches(matches)
                .build();
        
        // Создаем матчи с игроками
        List<MatchResponse> createdMatches = matchService.createMatches(request);
        
        // Обрабатываем автоматические баи (создаем отдельные матчи с одним игроком и автопобедой)
        List<Long> byePlayers = new ArrayList<>();
        for (int i = firstRoundRealMatches; i < participantsCount; i++) {
            byePlayers.add(participantIds.get(i));
        }
        
        for (Long playerId : byePlayers) {
            // Создаем матч с автопобедой
            BulkCreateMatchesRequest.MatchData matchData = new BulkCreateMatchesRequest.MatchData(
                    playerId, 
                    null); // null для второго игрока означает автопобеду
            
            BulkCreateMatchesRequest byeRequest = BulkCreateMatchesRequest.builder()
                    .tournamentId(tournamentId)
                    .roundNumber(1)
                    .matches(Collections.singletonList(matchData))
                    .build();
            
            // Создаем матч
            List<MatchResponse> byeMatch = matchService.createMatches(byeRequest);
            
            // Устанавливаем автопобеду в этом матче
            if (!byeMatch.isEmpty()) {
                MatchResultRequest resultRequest = MatchResultRequest.builder()
                        .matchId(byeMatch.get(0).getId())
                        .goalsUser1(3) // Стандартный счет для автопобеды
                        .goalsUser2(0)
                        .build();
                
                matchService.updateMatchResult(resultRequest);
            }
            
            createdMatches.addAll(byeMatch);
        }
        
        return createdMatches.size();
    }
    
    /**
     * Создает матчи для следующего раунда турнира
     * 
     * @param tournamentId ID турнира
     * @param currentRound Текущий раунд
     * @return Количество созданных матчей следующего раунда
     */
    @Transactional
    public int createNextRound(Long tournamentId, int currentRound) {
        // Получаем матчи текущего раунда
        List<MatchResponse> currentRoundMatches = matchService.getMatchesByTournamentAndRound(tournamentId, currentRound);
        
        if (currentRoundMatches.isEmpty()) {
            throw new TournamentException(
                    HttpStatus.NOT_FOUND, 
                    "Матчи текущего раунда не найдены: " + currentRound);
        }
        
        // Проверяем, завершены ли все матчи
        boolean allMatchesComplete = currentRoundMatches.stream()
                .allMatch(match -> match.getWinnerId() != null);
        
        if (!allMatchesComplete) {
            throw new TournamentException("Не все матчи текущего раунда завершены");
        }
        
        // Получаем победителей
        List<Long> winners = currentRoundMatches.stream()
                .map(MatchResponse::getWinnerId)
                .collect(Collectors.toList());
        
        // Если остался только один победитель, значит турнир завершен
        if (winners.size() == 1) {
            return 0; // Турнир завершен, нет следующего раунда
        }
        
        // Следующий раунд
        int nextRound = currentRound + 1;
        
        // Создаем матчи следующего раунда
        List<BulkCreateMatchesRequest.MatchData> nextRoundMatches = new ArrayList<>();
        
        // Формируем пары для следующего раунда
        for (int i = 0; i < winners.size(); i += 2) {
            Long userId1 = winners.get(i);
            // Проверяем, есть ли второй игрок или нужен бай
            Long userId2 = (i + 1 < winners.size()) ? winners.get(i + 1) : null;
            
            nextRoundMatches.add(new BulkCreateMatchesRequest.MatchData(userId1, userId2));
        }
        
        // Создаем запрос
        BulkCreateMatchesRequest request = BulkCreateMatchesRequest.builder()
                .tournamentId(tournamentId)
                .roundNumber(nextRound)
                .matches(nextRoundMatches)
                .build();
        
        // Создаем матчи
        List<MatchResponse> createdMatches = matchService.createMatches(request);
        
        // Если есть матчи с баем (где второй игрок null), устанавливаем автопобеду
        for (int i = 0; i < nextRoundMatches.size(); i++) {
            if (nextRoundMatches.get(i).getUserId2() == null && i < createdMatches.size()) {
                // Матч с баем
                MatchResultRequest resultRequest = MatchResultRequest.builder()
                        .matchId(createdMatches.get(i).getId())
                        .goalsUser1(3) // Стандартный счет для автопобеды
                        .goalsUser2(0)
                        .build();
                
                matchService.updateMatchResult(resultRequest);
            }
        }
        
        return createdMatches.size();
    }
    
    /**
     * Проверяет, является ли указанный раунд финальным
     * 
     * @param tournamentId ID турнира
     * @param roundNumber Номер раунда
     * @return true, если раунд финальный
     */
    public boolean isFinalRound(Long tournamentId, int roundNumber) {
        List<MatchResponse> matches = matchService.getMatchesByTournamentAndRound(tournamentId, roundNumber);
        return matches.size() == 1;
    }
    
    /**
     * Получает победителя турнира
     * 
     * @param tournamentId ID турнира
     * @return ID победителя или null, если финального матча нет или он не завершен
     */
    public Long getTournamentWinner(Long tournamentId) {
        // Получаем все матчи турнира
        List<MatchResponse> allMatches = matchService.getMatchesByTournament(tournamentId);
        
        if (allMatches.isEmpty()) {
            return null;
        }
        
        // Находим максимальный номер раунда
        int maxRound = allMatches.stream()
                .mapToInt(MatchResponse::getRoundNumber)
                .max()
                .orElse(0);
        
        // Получаем матчи финального раунда
        List<MatchResponse> finalMatches = allMatches.stream()
                .filter(match -> match.getRoundNumber() == maxRound)
                .collect(Collectors.toList());
        
        // В финале должен быть только один матч
        if (finalMatches.size() != 1) {
            return null;
        }
        
        // Возвращаем победителя финального матча
        return finalMatches.get(0).getWinnerId();
    }
}
