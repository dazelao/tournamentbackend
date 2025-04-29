package org.example.popitkan5.league.service;

import org.example.popitkan5.league.dto.BulkLeagueMatchRequest;
import org.example.popitkan5.league.dto.LeagueMatchResponse;
import org.example.popitkan5.league.dto.LeagueResultRequest;
import org.example.popitkan5.league.exception.LeagueException;
import org.example.popitkan5.league.model.League;
import org.example.popitkan5.league.model.LeagueMatch;
import org.example.popitkan5.league.model.LeagueParticipant;
import org.example.popitkan5.league.model.LeagueStatus;
import org.example.popitkan5.league.repository.LeagueMatchRepository;
import org.example.popitkan5.league.repository.LeagueParticipantRepository;
import org.example.popitkan5.league.repository.LeagueRepository;
import org.example.popitkan5.model.User;
import org.example.popitkan5.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class LeagueMatchService {

    private final LeagueMatchRepository matchRepository;
    private final LeagueRepository leagueRepository;
    private final LeagueParticipantRepository participantRepository;
    private final UserRepository userRepository;
    private final LeagueStatsService leagueStatsService;

    @Autowired
    public LeagueMatchService(LeagueMatchRepository matchRepository, 
                              LeagueRepository leagueRepository,
                              LeagueParticipantRepository participantRepository,
                              UserRepository userRepository,
                              LeagueStatsService leagueStatsService) {
        this.matchRepository = matchRepository;
        this.leagueRepository = leagueRepository;
        this.participantRepository = participantRepository;
        this.userRepository = userRepository;
        this.leagueStatsService = leagueStatsService;
    }

    /**
     * Создает матч между двумя участниками лиги
     */
    @Transactional
    public LeagueMatchResponse createMatch(Long userId1, Long userId2, Long leagueId, Integer roundNumber) {
        // Проверяем существование лиги
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new LeagueException("Лига с ID " + leagueId + " не найдена"));
        
        if (league.getStatus() != LeagueStatus.ACTIVE) {
            throw new LeagueException("Создание матчей возможно только для активных лиг");
        }
        
        // Проверяем, что оба пользователя являются участниками лиги
        if (!participantRepository.findByLeagueIdAndUserId(leagueId, userId1).isPresent() ||
            !participantRepository.findByLeagueIdAndUserId(leagueId, userId2).isPresent()) {
            throw new LeagueException("Один или оба пользователя не являются участниками лиги");
        }
        
        // Создаем матч
        LeagueMatch match = new LeagueMatch();
        match.setUserId1(userId1);
        match.setUserId2(userId2);
        match.setLeagueId(leagueId);
        match.setRoundNumber(roundNumber);
        
        match = matchRepository.save(match);
        
        return buildMatchResponse(match);
    }

    /**
     * Создает массово все матчи для лиги (каждый с каждым, 2 раза)
     */
    @Transactional
    public List<LeagueMatchResponse> generateAllMatches(Long leagueId) {
        // Проверяем существование лиги
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new LeagueException("Лига с ID " + leagueId + " не найдена"));
        
        if (league.getStatus() != LeagueStatus.ACTIVE) {
            throw new LeagueException("Генерация матчей возможно только для активных лиг");
        }
        
        // Получаем всех участников лиги
        List<LeagueParticipant> participants = participantRepository.findByLeagueId(leagueId);
        if (participants.size() < 2) {
            throw new LeagueException("Для генерации матчей необходимо минимум 2 участника");
        }
        
        List<LeagueMatch> matches = new ArrayList<>();
        List<Long> userIds = participants.stream()
                .map(LeagueParticipant::getUserId)
                .collect(Collectors.toList());
        
        // Генерируем матчи для первого круга (каждый с каждым)
        for (int i = 0; i < userIds.size(); i++) {
            for (int j = i + 1; j < userIds.size(); j++) {
                LeagueMatch match = new LeagueMatch();
                match.setUserId1(userIds.get(i));
                match.setUserId2(userIds.get(j));
                match.setLeagueId(leagueId);
                match.setRoundNumber(1);
                matches.add(match);
            }
        }
        
        // Генерируем матчи для второго круга (меняются домашние и гостевые команды)
        for (int i = 0; i < userIds.size(); i++) {
            for (int j = i + 1; j < userIds.size(); j++) {
                LeagueMatch match = new LeagueMatch();
                match.setUserId1(userIds.get(j)); // Меняем местами участников
                match.setUserId2(userIds.get(i));
                match.setLeagueId(leagueId);
                match.setRoundNumber(2);
                matches.add(match);
            }
        }
        
        // Сохраняем все матчи
        List<LeagueMatch> savedMatches = matchRepository.saveAll(matches);
        
        // Инициализируем таблицу истории лиги для всех участников
        for (Long userId : userIds) {
            leagueStatsService.initializeLeagueHistory(leagueId, userId, participants.size());
        }
        
        return savedMatches.stream()
                .map(this::buildMatchResponse)
                .collect(Collectors.toList());
    }

    /**
     * Создает несколько матчей для лиги
     */
    @Transactional
    public List<LeagueMatchResponse> createMatches(BulkLeagueMatchRequest request) {
        Long leagueId = request.getLeagueId();
        
        // Проверяем существование лиги
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new LeagueException("Лига с ID " + leagueId + " не найдена"));
        
        if (league.getStatus() != LeagueStatus.ACTIVE) {
            throw new LeagueException("Создание матчей возможно только для активных лиг");
        }
        
        List<LeagueMatch> matches = new ArrayList<>();
        
        for (BulkLeagueMatchRequest.MatchData matchData : request.getMatches()) {
            Long userId1 = matchData.getUserId1();
            Long userId2 = matchData.getUserId2();
            
            // Проверяем, что оба пользователя являются участниками лиги
            if (!participantRepository.findByLeagueIdAndUserId(leagueId, userId1).isPresent() ||
                !participantRepository.findByLeagueIdAndUserId(leagueId, userId2).isPresent()) {
                throw new LeagueException("Один или оба пользователя не являются участниками лиги: " + userId1 + ", " + userId2);
            }
            
            LeagueMatch match = new LeagueMatch();
            match.setUserId1(userId1);
            match.setUserId2(userId2);
            match.setLeagueId(leagueId);
            match.setRoundNumber(request.getRoundNumber());
            
            matches.add(match);
        }
        
        List<LeagueMatch> savedMatches = matchRepository.saveAll(matches);
        
        return savedMatches.stream()
                .map(this::buildMatchResponse)
                .collect(Collectors.toList());
    }

    /**
     * Получает список всех матчей лиги
     */
    public List<LeagueMatchResponse> getMatchesByLeague(Long leagueId) {
        List<LeagueMatch> matches = matchRepository.findByLeagueId(leagueId);
        
        // Загружаем информацию о пользователях для ответа
        Set<Long> userIds = new HashSet<>();
        for (LeagueMatch match : matches) {
            userIds.add(match.getUserId1());
            if (match.getUserId2() != null) {
                userIds.add(match.getUserId2());
            }
        }
        
        Map<Long, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, user -> user));
        
        return matches.stream()
                .map(match -> buildMatchResponseWithUsernames(match, userMap))
                .collect(Collectors.toList());
    }

    /**
     * Получает матчи пользователя
     */
    public List<LeagueMatchResponse> getUserMatches(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new LeagueException("Пользователь не найден"));

        List<LeagueMatch> matches = matchRepository.findByUserIdInAllLeagues(user.getId());

        // Загружаем информацию о пользователях для ответа
        Set<Long> userIds = new HashSet<>();
        for (LeagueMatch match : matches) {
            userIds.add(match.getUserId1());
            if (match.getUserId2() != null) {
                userIds.add(match.getUserId2());
            }
        }

        Map<Long, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        return matches.stream()
                .map(match -> buildMatchResponseWithUsernames(match, userMap))
                .collect(Collectors.toList());
    }

    /**
     * Получает информацию о матче по ID
     */
    public LeagueMatchResponse getMatch(Long matchId) {
        LeagueMatch match = matchRepository.findById(matchId)
                .orElseThrow(() -> new LeagueException("Матч с ID " + matchId + " не найден"));
        
        // Загружаем информацию о пользователях для ответа
        Set<Long> userIds = new HashSet<>();
        userIds.add(match.getUserId1());
        if (match.getUserId2() != null) {
            userIds.add(match.getUserId2());
        }
        
        Map<Long, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, user -> user));
        
        return buildMatchResponseWithUsernames(match, userMap);
    }

    /**
     * Обновляет результаты матча
     */
    @Transactional
    public LeagueMatchResponse updateMatchResult(Long matchId, LeagueResultRequest request) {
        // Проверяем, что ID матча в запросе совпадает с ID в пути
        if (request.getMatchId() != null && !request.getMatchId().equals(matchId)) {
            throw new LeagueException("Несоответствие ID матча в запросе и пути");
        }
        
        LeagueMatch match = matchRepository.findById(matchId)
                .orElseThrow(() -> new LeagueException("Матч с ID " + matchId + " не найден"));
        
        League league = leagueRepository.findById(match.getLeagueId())
                .orElseThrow(() -> new LeagueException("Лига не найдена"));
        
        if (league.getStatus() != LeagueStatus.ACTIVE) {
            throw new LeagueException("Обновление результатов возможно только для активных лиг");
        }
        
        // Устанавливаем результаты матча
        match.setGoalsUser1(request.getGoalsUser1());
        match.setGoalsUser2(request.getGoalsUser2());
        
        // Определяем победителя и проигравшего
        if (request.getGoalsUser1() > request.getGoalsUser2()) {
            match.setWinnerId(match.getUserId1());
            match.setLoserId(match.getUserId2());
            match.setResultUser1("WIN");
            match.setResultUser2("LOSE");
            match.setIsDraw(false);
        } else if (request.getGoalsUser1() < request.getGoalsUser2()) {
            match.setWinnerId(match.getUserId2());
            match.setLoserId(match.getUserId1());
            match.setResultUser1("LOSE");
            match.setResultUser2("WIN");
            match.setIsDraw(false);
        } else {
            // Ничья
            match.setWinnerId(null);
            match.setLoserId(null);
            match.setResultUser1("DRAW");
            match.setResultUser2("DRAW");
            match.setIsDraw(true);
        }
        
        match = matchRepository.save(match);
        
        // Обновляем статистику и таблицу лиги
        leagueStatsService.updateStats(matchId);
        
        return buildMatchResponse(match);
    }

    /**
     * Формирует ответ с информацией о матче
     */
    private LeagueMatchResponse buildMatchResponse(LeagueMatch match) {
        return LeagueMatchResponse.builder()
                .id(match.getId())
                .userId1(match.getUserId1())
                .userId2(match.getUserId2())
                .goalsUser1(match.getGoalsUser1())
                .goalsUser2(match.getGoalsUser2())
                .resultUser1(match.getResultUser1())
                .resultUser2(match.getResultUser2())
                .createdDate(match.getCreatedDate())
                .modifiedDate(match.getModifiedDate())
                .leagueId(match.getLeagueId())
                .roundNumber(match.getRoundNumber())
                .winnerId(match.getWinnerId())
                .loserId(match.getLoserId())
                .isDraw(match.getIsDraw())
                .build();
    }

    /**
     * Формирует ответ с информацией о матче, включая имена пользователей
     */
    private LeagueMatchResponse buildMatchResponseWithUsernames(LeagueMatch match, Map<Long, User> userMap) {
        String username1 = userMap.containsKey(match.getUserId1()) ? userMap.get(match.getUserId1()).getUsername() : "Unknown";
        String username2 = match.getUserId2() != null && userMap.containsKey(match.getUserId2()) ? userMap.get(match.getUserId2()).getUsername() : "Unknown";
        
        return LeagueMatchResponse.builder()
                .id(match.getId())
                .userId1(match.getUserId1())
                .userId2(match.getUserId2())
                .username1(username1)
                .username2(username2)
                .goalsUser1(match.getGoalsUser1())
                .goalsUser2(match.getGoalsUser2())
                .resultUser1(match.getResultUser1())
                .resultUser2(match.getResultUser2())
                .createdDate(match.getCreatedDate())
                .modifiedDate(match.getModifiedDate())
                .leagueId(match.getLeagueId())
                .roundNumber(match.getRoundNumber())
                .winnerId(match.getWinnerId())
                .loserId(match.getLoserId())
                .isDraw(match.getIsDraw())
                .build();
    }
}
