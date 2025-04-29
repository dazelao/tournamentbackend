package org.example.popitkan5.tournament.service;

import lombok.RequiredArgsConstructor;
import org.example.popitkan5.model.User;
import org.example.popitkan5.service.UserService;
import org.example.popitkan5.tournament.dto.TournamentResponse;
import org.example.popitkan5.tournament.model.Tournament;
import org.example.popitkan5.tournament.model.TournamentParticipant;
import org.example.popitkan5.tournament.repository.TournamentParticipantRepository;
import org.example.popitkan5.tournament.repository.TournamentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserTournamentService {

    private final TournamentRepository tournamentRepository;
    private final TournamentParticipantRepository participantRepository;
    private final UserService userService;

    /**
     * Получает список всех турниров, в которых участвует текущий пользователь
     */
    public List<TournamentResponse> getCurrentUserTournaments() {
        // Получаем текущего пользователя
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Пользователь не авторизован");
        }
        
        String username = ((UserDetails) authentication.getPrincipal()).getUsername();
        User user = userService.getUserByUsername(username)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден: " + username));
        Long userId = user.getId();

        return getUserTournaments(userId);
    }

    /**
     * Получает список всех турниров, в которых участвует указанный пользователь
     * 
     * @param userId ID пользователя
     * @return Список турниров
     */
    public List<TournamentResponse> getUserTournaments(Long userId) {
        // Получаем список участий пользователя в турнирах
        List<TournamentParticipant> participations = participantRepository.findAll()
                .stream()
                .filter(participant -> participant.getUserId().equals(userId))
                .collect(Collectors.toList());

        // Если нет участий, возвращаем пустой список
        if (participations.isEmpty()) {
            return new ArrayList<>();
        }

        // Собираем идентификаторы турниров
        List<Long> tournamentIds = participations.stream()
                .map(TournamentParticipant::getTournamentId)
                .collect(Collectors.toList());

        // Получаем информацию о турнирах
        List<Tournament> tournaments = tournamentRepository.findAllById(tournamentIds);

        // Формируем ответ
        return tournaments.stream()
                .map(tournament -> {
                    int participantsCount = participantRepository.countByTournamentId(tournament.getId());
                    return convertToTournamentResponse(tournament, participantsCount);
                })
                .collect(Collectors.toList());
    }

    /**
     * Преобразует сущность Tournament в DTO для ответа
     */
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
