package org.example.popitkan5.games.service;

import org.example.popitkan5.games.dto.BulkCreateMatchesRequest;
import org.example.popitkan5.games.dto.CreateMatchRequest;
import org.example.popitkan5.games.dto.MatchResponse;
import org.example.popitkan5.games.dto.MatchResultRequest;
import org.example.popitkan5.games.model.Match;
import org.example.popitkan5.games.repository.MatchRepository;
import org.example.popitkan5.dto.UserDto;
import org.example.popitkan5.model.User;
import org.example.popitkan5.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MatchService {

    private final MatchRepository matchRepository;
    private final UserRepository userRepository;

    @Autowired
    public MatchService(MatchRepository matchRepository, UserRepository userRepository) {
        this.matchRepository = matchRepository;
        this.userRepository = userRepository;
    }

    /**
     * Создает новый матч
     */
    @Transactional
    public MatchResponse createMatch(CreateMatchRequest request) {
        Match match = new Match();
        match.setUserId1(request.getUserId1());
        match.setUserId2(request.getUserId2());
        match.setTournamentId(request.getTournamentId());
        match.setRoundNumber(request.getRoundNumber());
        // Дата создания будет установлена автоматически через @PrePersist

        Match savedMatch = matchRepository.save(match);
        return convertToMatchResponse(savedMatch);
    }
    
    /**
     * Создает матч с автопобедой (бай) для турнира
     * Используется для Single Elimination турниров, когда число участников нечетное
     */
    @Transactional
    public MatchResponse createByeMatch(CreateMatchRequest request) {
        Match match = new Match();
        match.setUserId1(request.getUserId1());
        match.setUserId2(null); // null означает бай/автопобеду
        match.setTournamentId(request.getTournamentId());
        match.setRoundNumber(request.getRoundNumber());

        // Автоматически устанавливаем победителя для бай-матча
        match.setWinnerId(request.getUserId1());
        match.setLoserId(null);
        match.setGoalsUser1(3); // Стандартный счет для автопобеды
        match.setGoalsUser2(0);
        match.setResultUser1("победа");
        match.setResultUser2(null);
        match.setModifiedDate(LocalDateTime.now()); // Сразу отмечаем как завершенный

        Match savedMatch = matchRepository.save(match);
        return convertToMatchResponse(savedMatch);
    }
    
    /**
     * Массовое создание матчей
     */
    @Transactional
    public List<MatchResponse> createMatches(BulkCreateMatchesRequest request) {
        List<Match> matches = new ArrayList<>();
        
        for (BulkCreateMatchesRequest.MatchData matchData : request.getMatches()) {
            Match match = new Match();
            match.setUserId1(matchData.getUserId1());
            match.setUserId2(matchData.getUserId2());
            match.setTournamentId(request.getTournamentId());
            match.setRoundNumber(request.getRoundNumber());
            
            // Если второй участник null, это автоматическая победа (бай)
            if (matchData.getUserId2() == null) {
                // Устанавливаем автопобеду
                match.setWinnerId(matchData.getUserId1());
                match.setLoserId(null);
                match.setGoalsUser1(3); // Стандартный счет для автопобеды
                match.setGoalsUser2(0);
                match.setResultUser1("победа");
                match.setResultUser2(null);
                match.setModifiedDate(LocalDateTime.now()); // Сразу отмечаем как завершенный
            }
            
            matches.add(match);
        }

        List<Match> savedMatches = matchRepository.saveAll(matches);
        return savedMatches.stream()
                .map(this::convertToMatchResponse)
                .collect(Collectors.toList());
    }

    /**
     * Получает матч по ID
     */
    public MatchResponse getMatchById(Long id) {
        Match match = matchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Матч не найден с ID: " + id));
        return convertToMatchResponse(match);
    }

    /**
     * Обновляет результат матча
     */
    @Transactional
    public MatchResponse updateMatchResult(MatchResultRequest request) {
        Match match = matchRepository.findById(request.getMatchId())
                .orElseThrow(() -> new RuntimeException("Матч не найден с ID: " + request.getMatchId()));

        // Устанавливаем голы
        match.setGoalsUser1(request.getGoalsUser1());
        match.setGoalsUser2(request.getGoalsUser2() != null ? request.getGoalsUser2() : 0);
        
        // Обновляем турнир и раунд, если указаны
        if (request.getTournamentId() != null) {
            match.setTournamentId(request.getTournamentId());
        }
        
        if (request.getRoundNumber() != null) {
            match.setRoundNumber(request.getRoundNumber());
        }

        // Определяем результат матча
        int goalDifference = request.getGoalsUser1() - (request.getGoalsUser2() != null ? request.getGoalsUser2() : 0);
        
        // Устанавливаем дату модификации
        match.setModifiedDate(LocalDateTime.now());

        if (goalDifference > 0) {
            // Игрок 1 выиграл
            match.setResultUser1("победа");
            match.setResultUser2("поражение");
            match.setWinnerId(match.getUserId1());
            match.setLoserId(match.getUserId2());
            match.setDrawUser1(null);
            match.setDrawUser2(null);
        } else if (goalDifference < 0) {
            // Игрок 2 выиграл
            match.setResultUser1("поражение");
            match.setResultUser2("победа");
            match.setWinnerId(match.getUserId2());
            match.setLoserId(match.getUserId1());
            match.setDrawUser1(null);
            match.setDrawUser2(null);
        } else {
            // Ничья
            match.setResultUser1("ничья");
            match.setResultUser2("ничья");
            match.setWinnerId(null);
            match.setLoserId(null);
            match.setDrawUser1(match.getUserId1());
            match.setDrawUser2(match.getUserId2());
        }

        Match updatedMatch = matchRepository.save(match);
        return convertToMatchResponse(updatedMatch);
    }

    /**
     * Получает все матчи для турнира
     */
    public List<MatchResponse> getMatchesByTournament(Long tournamentId) {
        List<Match> matches = matchRepository.findByTournamentId(tournamentId);
        return matches.stream()
                .map(this::convertToMatchResponse)
                .collect(Collectors.toList());
    }

    /**
     * Получает все матчи для турнира с информацией о пользователях
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getMatchesWithUsersByTournament(Long tournamentId) {
        Map<String, Object> result = new HashMap<>();
        
        // Получить все матчи турнира
        List<Match> matches = matchRepository.findByTournamentId(tournamentId);
        List<MatchResponse> matchResponses = matches.stream()
                .map(this::convertToMatchResponse)
                .collect(Collectors.toList());
        
        // Собрать ID всех пользователей из матчей
        Set<Long> userIds = new HashSet<>();
        matches.forEach(match -> {
            if (match.getUserId1() != null) userIds.add(match.getUserId1());
            if (match.getUserId2() != null) userIds.add(match.getUserId2());
            if (match.getWinnerId() != null) userIds.add(match.getWinnerId());
            if (match.getLoserId() != null) userIds.add(match.getLoserId());
            if (match.getDrawUser1() != null) userIds.add(match.getDrawUser1());
            if (match.getDrawUser2() != null) userIds.add(match.getDrawUser2());
        });
        
        // Получить всех пользователей одним запросом
        List<User> users = userRepository.findAllById(userIds);
        Map<Long, UserDto> userDtoMap = new HashMap<>();
        
        users.forEach(user -> {
            UserDto userDto = new UserDto();
            userDto.setId(user.getId());
            userDto.setUsername(user.getUsername());
            // Email не используется в модели User, убираем эту строку
            userDtoMap.put(user.getId(), userDto);
        });
        
        result.put("matches", matchResponses);
        result.put("users", userDtoMap);
        
        return result;
    }

    /**
     * Получает все матчи для турнира и раунда
     */
    public List<MatchResponse> getMatchesByTournamentAndRound(Long tournamentId, Integer roundNumber) {
        List<Match> matches = matchRepository.findByTournamentIdAndRoundNumber(tournamentId, roundNumber);
        return matches.stream()
                .map(this::convertToMatchResponse)
                .collect(Collectors.toList());
    }

    /**
     * Получает все матчи для игрока
     */
    public List<MatchResponse> getMatchesByPlayer(Long userId) {
        List<Match> matches = matchRepository.findByUserId1OrUserId2(userId, userId);
        return matches.stream()
                .map(this::convertToMatchResponse)
                .collect(Collectors.toList());
    }

    /**
     * Конвертирует модель Match в DTO MatchResponse
     */
    private MatchResponse convertToMatchResponse(Match match) {
        String matchStatus = "создан";
        if (match.getModifiedDate() != null) {
            matchStatus = "завершен";
        }
        
        return MatchResponse.builder()
                .id(match.getId())
                .userId1(match.getUserId1())
                .userId2(match.getUserId2())
                .goalsUser1(match.getGoalsUser1())
                .goalsUser2(match.getGoalsUser2())
                .resultUser1(match.getResultUser1())
                .resultUser2(match.getResultUser2())
                .createdDate(match.getCreatedDate())
                .modifiedDate(match.getModifiedDate())
                .tournamentId(match.getTournamentId())
                .roundNumber(match.getRoundNumber())
                .winnerId(match.getWinnerId())
                .loserId(match.getLoserId())
                .drawUser1(match.getDrawUser1())
                .drawUser2(match.getDrawUser2())
                .matchStatus(matchStatus)
                .build();
    }
}