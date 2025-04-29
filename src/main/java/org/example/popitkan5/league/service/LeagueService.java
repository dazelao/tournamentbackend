package org.example.popitkan5.league.service;

import org.example.popitkan5.league.dto.*;
import org.example.popitkan5.league.exception.LeagueException;
import org.example.popitkan5.league.model.League;
import org.example.popitkan5.league.model.LeagueParticipant;
import org.example.popitkan5.league.model.LeagueStatus;
import org.example.popitkan5.league.repository.LeagueParticipantRepository;
import org.example.popitkan5.league.repository.LeagueRepository;
import org.example.popitkan5.model.User;
import org.example.popitkan5.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LeagueService {

    private final LeagueRepository leagueRepository;
    private final LeagueParticipantRepository participantRepository;
    private final UserRepository userRepository;

    @Autowired
    public LeagueService(LeagueRepository leagueRepository, 
                         LeagueParticipantRepository participantRepository,
                         UserRepository userRepository) {
        this.leagueRepository = leagueRepository;
        this.participantRepository = participantRepository;
        this.userRepository = userRepository;
    }

    /**
     * Создает новую лигу
     */
    @Transactional
    public LeagueResponse createLeague(CreateLeagueRequest request) {
        League league = new League();
        league.setName(request.getName());
        league.setMaxParticipants(request.getMaxParticipants());
        league.setStatus(LeagueStatus.DRAFT);
        
        // Устанавливаем атрибуты, если они предоставлены
        league.setWinnerAttribute(request.getWinnerAttribute());
        league.setLoserAttribute(request.getLoserAttribute());
        league.setSaveAttribute(request.getSaveAttribute());
        league.setWinnerCount(request.getWinnerCount());
        league.setLoserCount(request.getLoserCount());
        league.setSaveCount(request.getSaveCount());
        
        // Сохраняем лигу
        league = leagueRepository.save(league);
        
        // Устанавливаем правильный leagueId после сохранения
        league.setLeagueId("L" + league.getId());
        league = leagueRepository.save(league);
        
        return buildLeagueResponse(league);
    }

    /**
     * Получает информацию о лиге по ID
     */
    public LeagueResponse getLeague(Long id) {
        League league = leagueRepository.findById(id)
                .orElseThrow(() -> new LeagueException("Лига с ID " + id + " не найдена"));
        
        return buildLeagueResponse(league);
    }

    /**
     * Получает список всех лиг
     */
    public List<LeagueResponse> getAllLeagues() {
        return leagueRepository.findAll().stream()
                .map(this::buildLeagueResponse)
                .collect(Collectors.toList());
    }

    /**
     * Переводит лигу в статус регистрации
     */
    @Transactional
    public LeagueResponse startRegistration(Long leagueId) {
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new LeagueException("Лига с ID " + leagueId + " не найдена"));
        
        if (league.getStatus() != LeagueStatus.DRAFT) {
            throw new LeagueException("Лига должна быть в статусе DRAFT для начала регистрации");
        }
        
        league.setStatus(LeagueStatus.REGISTRATION);
        league = leagueRepository.save(league);
        
        return buildLeagueResponse(league);
    }

    /**
     * Запускает лигу (переводит в статус ACTIVE)
     */
    @Transactional
    public LeagueResponse startLeague(Long leagueId) {
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new LeagueException("Лига с ID " + leagueId + " не найдена"));
        
        if (league.getStatus() != LeagueStatus.REGISTRATION) {
            throw new LeagueException("Лига должна быть в статусе REGISTRATION для запуска");
        }
        
        int participantCount = participantRepository.countByLeagueId(leagueId);
        if (participantCount < 2) {
            throw new LeagueException("Для запуска лиги необходимо минимум 2 участника");
        }
        
        league.setStatus(LeagueStatus.ACTIVE);
        league = leagueRepository.save(league);
        
        return buildLeagueResponse(league);
    }

    /**
     * Завершает лигу и раздает атрибуты участникам
     */
    @Transactional
    public LeagueResponse finishLeague(Long leagueId) {
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new LeagueException("Лига с ID " + leagueId + " не найдена"));
        
        if (league.getStatus() != LeagueStatus.ACTIVE) {
            throw new LeagueException("Лига должна быть в статусе ACTIVE для завершения");
        }
        
        // Логика раздачи атрибутов будет реализована в сервисе LeagueStatsService
        
        league.setStatus(LeagueStatus.FINISHED);
        league = leagueRepository.save(league);
        
        return buildLeagueResponse(league);
    }

    /**
     * Отменяет лигу
     */
    @Transactional
    public LeagueResponse cancelLeague(Long leagueId) {
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new LeagueException("Лига с ID " + leagueId + " не найдена"));
        
        if (league.getStatus() == LeagueStatus.FINISHED) {
            throw new LeagueException("Невозможно отменить завершенную лигу");
        }
        
        league.setStatus(LeagueStatus.CANCELED);
        league = leagueRepository.save(league);
        
        return buildLeagueResponse(league);
    }

    /**
     * Добавляет участника в лигу
     */
    @Transactional
    public LeagueResponse addParticipant(Long leagueId, Long userId) {
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new LeagueException("Лига с ID " + leagueId + " не найдена"));
        
        if (league.getStatus() != LeagueStatus.REGISTRATION) {
            throw new LeagueException("Добавление участников возможно только в статусе REGISTRATION");
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new LeagueException("Пользователь с ID " + userId + " не найден"));
        
        // Проверяем, не зарегистрирован ли уже пользователь
        Optional<LeagueParticipant> existingParticipant = participantRepository.findByLeagueIdAndUserId(leagueId, userId);
        if (existingParticipant.isPresent()) {
            throw new LeagueException("Пользователь уже зарегистрирован в лиге");
        }
        
        // Проверяем, не достигнуто ли максимальное количество участников
        int currentParticipants = participantRepository.countByLeagueId(leagueId);
        if (currentParticipants >= league.getMaxParticipants()) {
            throw new LeagueException("Достигнуто максимальное количество участников");
        }
        
        // Добавляем участника
        LeagueParticipant participant = new LeagueParticipant();
        participant.setLeagueId(leagueId);
        participant.setUserId(userId);
        participantRepository.save(participant);
        
        return buildLeagueResponse(league);
    }

    /**
     * Добавляет список участников в лигу
     */
    @Transactional
    public LeagueResponse addParticipantsByAttribute(Long leagueId, BulkParticipantRequest request) {
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new LeagueException("Лига с ID " + leagueId + " не найдена"));
        
        if (league.getStatus() != LeagueStatus.REGISTRATION) {
            throw new LeagueException("Добавление участников возможно только в статусе REGISTRATION");
        }
        
        List<Long> userIds = request.getUserIds();
        String attribute = request.getAttribute();
        
        List<User> users;
        if (attribute != null && !attribute.isEmpty()) {
            // Получаем пользователей с указанным атрибутом
            users = userRepository.findAllById(userIds).stream()
                    .filter(user -> user.getAttributes().containsKey(attribute))
                    .collect(Collectors.toList());
        } else {
            // Если атрибут не указан, добавляем всех пользователей из списка
            users = userRepository.findAllById(userIds);
        }
        
        int currentParticipants = participantRepository.countByLeagueId(leagueId);
        int availableSlots = league.getMaxParticipants() - currentParticipants;
        
        if (users.size() > availableSlots) {
            throw new LeagueException("Недостаточно мест для всех участников. Доступно: " + availableSlots);
        }
        
        // Добавляем участников
        for (User user : users) {
            Optional<LeagueParticipant> existingParticipant = participantRepository.findByLeagueIdAndUserId(leagueId, user.getId());
            if (!existingParticipant.isPresent()) {
                LeagueParticipant participant = new LeagueParticipant();
                participant.setLeagueId(leagueId);
                participant.setUserId(user.getId());
                participantRepository.save(participant);
            }
        }
        
        return buildLeagueResponse(league);
    }

    /**
     * Удаляет участника из лиги
     */
    @Transactional
    public LeagueResponse removeParticipant(Long leagueId, Long userId) {
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new LeagueException("Лига с ID " + leagueId + " не найдена"));
        
        if (league.getStatus() != LeagueStatus.REGISTRATION && league.getStatus() != LeagueStatus.DRAFT) {
            throw new LeagueException("Удаление участников возможно только в статусе DRAFT или REGISTRATION");
        }
        
        Optional<LeagueParticipant> participant = participantRepository.findByLeagueIdAndUserId(leagueId, userId);
        if (!participant.isPresent()) {
            throw new LeagueException("Участник не найден в лиге");
        }
        
        participantRepository.deleteByLeagueIdAndUserId(leagueId, userId);
        
        return buildLeagueResponse(league);
    }

    /**
     * Формирует ответ с информацией о лиге
     */
    /**
     * Получает список участников лиги
     */
    public List<User> getLeagueParticipants(Long leagueId) {
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new LeagueException("Лига с ID " + leagueId + " не найдена"));
        
        List<LeagueParticipant> participants = participantRepository.findByLeagueId(leagueId);
        List<Long> userIds = participants.stream()
                .map(LeagueParticipant::getUserId)
                .collect(Collectors.toList());
        
        if (userIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        return userRepository.findAllById(userIds);
    }

    /**
     * Формирует ответ с информацией о лиге
     */
    private LeagueResponse buildLeagueResponse(League league) {
        int participantCount = participantRepository.countByLeagueId(league.getId());
        
        return LeagueResponse.builder()
                .id(league.getId())
                .leagueId(league.getLeagueId())
                .name(league.getName())
                .maxParticipants(league.getMaxParticipants())
                .status(league.getStatus())
                .createdDate(league.getCreatedDate())
                .modifiedDate(league.getModifiedDate())
                .currentParticipants(participantCount)
                .winnerAttribute(league.getWinnerAttribute())
                .loserAttribute(league.getLoserAttribute())
                .saveAttribute(league.getSaveAttribute())
                .winnerCount(league.getWinnerCount())
                .loserCount(league.getLoserCount())
                .saveCount(league.getSaveCount())
                .build();
    }
}
