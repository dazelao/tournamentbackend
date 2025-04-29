package org.example.popitkan5.tournament.service;

import org.example.popitkan5.games.dto.BulkCreateMatchesRequest;
import org.example.popitkan5.games.dto.MatchResponse;
import org.example.popitkan5.games.service.MatchService;
import org.example.popitkan5.model.User;
import org.example.popitkan5.repository.UserRepository;
import org.example.popitkan5.tournament.dto.AddParticipantRequest;
import org.example.popitkan5.tournament.dto.CreateTournamentRequest;
import org.example.popitkan5.tournament.dto.TournamentResponse;
import org.example.popitkan5.tournament.model.Tournament;
import org.example.popitkan5.tournament.model.TournamentParticipant;
import org.example.popitkan5.tournament.model.TournamentStatus;
import org.example.popitkan5.tournament.repository.TournamentParticipantRepository;
import org.example.popitkan5.tournament.repository.TournamentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TournamentService {

    private final TournamentRepository tournamentRepository;
    private final TournamentParticipantRepository participantRepository;
    private final UserRepository userRepository;
    private final MatchService matchService;
    private final TournamentBracketService bracketService;

    @Autowired
    public TournamentService(
            TournamentRepository tournamentRepository,
            TournamentParticipantRepository participantRepository,
            UserRepository userRepository,
            MatchService matchService,
            TournamentBracketService bracketService) {
        this.tournamentRepository = tournamentRepository;
        this.participantRepository = participantRepository;
        this.userRepository = userRepository;
        this.matchService = matchService;
        this.bracketService = bracketService;
    }

    /**
     * Создает новый турнир
     */
    @Transactional
    public TournamentResponse createTournament(CreateTournamentRequest request) {
        Tournament tournament = new Tournament();
        tournament.setName(request.getName());
        tournament.setDescription(request.getDescription());
        tournament.setMaxParticipants(request.getMaxParticipants());
        tournament.setStatus(TournamentStatus.DRAFT);
        tournament.setCurrentRound(0);
        
        Tournament savedTournament = tournamentRepository.save(tournament);
        
        return convertToTournamentResponse(savedTournament, 0);
    }

    /**
     * Получает информацию о турнире по ID
     */
    public TournamentResponse getTournamentById(Long id) {
        Tournament tournament = findTournamentOrThrow(id);
        int participantsCount = participantRepository.countByTournamentId(id);
        
        return convertToTournamentResponse(tournament, participantsCount);
    }
    
    /**
     * Получает список всех турниров
     */
    public List<TournamentResponse> getAllTournaments() {
        List<Tournament> tournaments = tournamentRepository.findAll();
        
        return tournaments.stream()
                .map(tournament -> {
                    int participantsCount = participantRepository.countByTournamentId(tournament.getId());
                    return convertToTournamentResponse(tournament, participantsCount);
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Получает список турниров по статусу
     */
    public List<TournamentResponse> getTournamentsByStatus(TournamentStatus status) {
        List<Tournament> tournaments = tournamentRepository.findByStatus(status);
        
        return tournaments.stream()
                .map(tournament -> {
                    int participantsCount = participantRepository.countByTournamentId(tournament.getId());
                    return convertToTournamentResponse(tournament, participantsCount);
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Добавляет участника в турнир
     */
    @Transactional
    public TournamentResponse addParticipant(Long tournamentId, AddParticipantRequest request) {
        Tournament tournament = findTournamentOrThrow(tournamentId);
        
        // Проверка существования пользователя
        userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Пользователь не найден с ID: " + request.getUserId()));
        
        // Проверка статуса турнира
        if (tournament.getStatus() != TournamentStatus.DRAFT) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Нельзя добавить участника, турнир уже запущен");
        }
        
        // Проверка количества участников
        int currentParticipants = participantRepository.countByTournamentId(tournamentId);
        if (currentParticipants >= tournament.getMaxParticipants()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Турнир уже заполнен");
        }
        
        // Проверка на дубликат
        if (participantRepository.existsByTournamentIdAndUserId(tournamentId, request.getUserId())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Пользователь уже зарегистрирован в турнире");
        }
        
        // Добавление участника
        TournamentParticipant participant = new TournamentParticipant();
        participant.setTournamentId(tournamentId);
        participant.setUserId(request.getUserId());
        participantRepository.save(participant);
        
        // Возвращаем обновленную информацию о турнире
        currentParticipants++; // Инкрементируем счетчик
        return convertToTournamentResponse(tournament, currentParticipants);
    }
    
    /**
     * Удаляет участника из турнира
     */
    @Transactional
    public TournamentResponse removeParticipant(Long tournamentId, Long userId) {
        Tournament tournament = findTournamentOrThrow(tournamentId);
        
        // Проверка статуса турнира
        if (tournament.getStatus() != TournamentStatus.DRAFT) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Нельзя удалить участника, турнир уже запущен");
        }
        
        // Удаление участника
        participantRepository.deleteByTournamentIdAndUserId(tournamentId, userId);
        
        // Возвращаем обновленную информацию о турнире
        int currentParticipants = participantRepository.countByTournamentId(tournamentId);
        return convertToTournamentResponse(tournament, currentParticipants);
    }
    
    /**
     * Запускает турнир
     */
    @Transactional
    public TournamentResponse startTournament(Long id) {
        Tournament tournament = findTournamentOrThrow(id);
        
        // Проверка статуса
        if (tournament.getStatus() != TournamentStatus.DRAFT) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Турнир уже запущен или завершен");
        }
        
        // Получение списка участников
        List<TournamentParticipant> participants = participantRepository.findByTournamentId(id);
        int participantsCount = participants.size();
        
        // Проверка минимального количества участников
        if (participantsCount < 2) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Для запуска турнира нужно минимум 2 участника");
        }
        
        // Перемешиваем участников для случайного распределения
        List<Long> participantIds = participants.stream()
                .map(TournamentParticipant::getUserId)
                .collect(Collectors.toList());
        Collections.shuffle(participantIds);
        
        // Создаем первый раунд матчей
        bracketService.createFirstRound(tournament.getId(), participantIds);
        
        // Обновляем статус турнира
        tournament.setStatus(TournamentStatus.IN_PROGRESS);
        tournament.setStartDate(LocalDateTime.now());
        tournament.setCurrentRound(1);
        
        Tournament updatedTournament = tournamentRepository.save(tournament);
        
        return convertToTournamentResponse(updatedTournament, participantsCount);
    }
    
    /**
     * Генерирует следующий раунд турнира
     */
    @Transactional
    public TournamentResponse generateNextRound(Long id) {
        Tournament tournament = findTournamentOrThrow(id);
        
        // Проверка статуса
        if (tournament.getStatus() != TournamentStatus.IN_PROGRESS) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Турнир не находится в процессе");
        }
        
        int currentRound = tournament.getCurrentRound();
        
        try {
            // Пытаемся создать следующий раунд
            int nextRound = currentRound + 1;
            int createdMatches = bracketService.createNextRound(id, currentRound);
            
            // Если нет созданных матчей, значит турнир завершен (остался один победитель)
            if (createdMatches == 0) {
                tournament.setStatus(TournamentStatus.FINISHED);
                Tournament finishedTournament = tournamentRepository.save(tournament);
                int participantsCount = participantRepository.countByTournamentId(id);
                return convertToTournamentResponse(finishedTournament, participantsCount);
            }
            
            // Обновляем номер текущего раунда
            tournament.setCurrentRound(nextRound);
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Ошибка при создании следующего раунда: " + e.getMessage());
        }
        
        Tournament updatedTournament = tournamentRepository.save(tournament);
        
        int participantsCount = participantRepository.countByTournamentId(id);
        return convertToTournamentResponse(updatedTournament, participantsCount);
    }
    
    /**
     * Завершает турнир
     */
    @Transactional
    public TournamentResponse finishTournament(Long id) {
        Tournament tournament = findTournamentOrThrow(id);
        
        // Проверка статуса
        if (tournament.getStatus() != TournamentStatus.IN_PROGRESS) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Турнир не находится в процессе");
        }
        
        int currentRound = tournament.getCurrentRound();
        
        // Проверяем, является ли текущий раунд финальным
        if (!bracketService.isFinalRound(id, currentRound)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Текущий раунд не является финальным");
        }
        
        // Проверяем, определен ли победитель турнира
        Long winnerId = bracketService.getTournamentWinner(id);
        if (winnerId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Финальный матч еще не завершен");
        }
        
        // Обновляем статус турнира
        tournament.setStatus(TournamentStatus.FINISHED);
        Tournament finishedTournament = tournamentRepository.save(tournament);
        
        int participantsCount = participantRepository.countByTournamentId(id);
        return convertToTournamentResponse(finishedTournament, participantsCount);
    }
    
    /**
     * Получает список участников турнира
     * 
     * @param tournamentId ID турнира
     * @return Список пользователей-участников
     */
    public List<User> getTournamentParticipants(Long tournamentId) {
        // Проверяем существование турнира
        findTournamentOrThrow(tournamentId);
        
        // Получаем всех участников
        List<TournamentParticipant> participants = participantRepository.findByTournamentId(tournamentId);
        
        // Получаем информацию о пользователях
        return participants.stream()
                .map(participant -> userRepository.findById(participant.getUserId())
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "Пользователь не найден с ID: " + participant.getUserId())))
                .collect(Collectors.toList());
    }
    
    // Вспомогательные методы
    
    private Tournament findTournamentOrThrow(Long id) {
        return tournamentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Турнир не найден с ID: " + id));
    }
    
    private TournamentResponse convertToTournamentResponse(Tournament tournament, int participantsCount) {
        return TournamentResponse.builder()
                .id(tournament.getId())
                .name(tournament.getName())
                .description(tournament.getDescription())
                .status(tournament.getStatus())
                .maxParticipants(tournament.getMaxParticipants())
                .startDate(tournament.getStartDate())
                .modifiedAt(tournament.getModifiedAt())
                .currentRound(tournament.getCurrentRound())
                .participantsCount(participantsCount)
                .build();
    }
}
