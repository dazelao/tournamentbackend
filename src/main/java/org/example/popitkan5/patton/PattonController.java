package org.example.popitkan5.patton;

import org.example.popitkan5.patton.dto.CreateGroupsRequest;
import org.example.popitkan5.Swizz.model.PlayerResult;
import org.example.popitkan5.Swizz.service.SwissTournamentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/patton")
public class PattonController {

    private final GroupingService groupingService;
    private final SwissTournamentService tournamentService;

    @Autowired
    public PattonController(GroupingService groupingService, SwissTournamentService tournamentService) {
        this.groupingService = groupingService;
        this.tournamentService = tournamentService;
    }

    /**
     * Создает группы на основе результатов турнира
     * @param request объект с параметрами запроса
     * @return Распределение пользователей по группам
     */
    @PostMapping("/create-groups-dto")
    public ResponseEntity<?> createGroupsWithDto(@RequestBody CreateGroupsRequest request) {
        try {
            // Получаем результаты турнира
            List<Map<String, Object>> results = new ArrayList<>();
            // Преобразуем PlayerResult в Map для упрощения работы с данными
            tournamentService.getTournamentResults(request.getTournamentId()).forEach(result -> {
                Map<String, Object> resultMap = new HashMap<>();
                resultMap.put("id", result.getId());
                resultMap.put("userId", result.getUser().getId());
                resultMap.put("username", result.getUser().getUsername());
                resultMap.put("points", result.getPoints());
                resultMap.put("wins", result.getWins());
                resultMap.put("losses", result.getLosses());
                resultMap.put("draws", result.getDraws());
                resultMap.put("goalsFor", result.getGoalsFor());
                resultMap.put("goalsAgainst", result.getGoalsAgainst());
                resultMap.put("goalDifference", result.getGoalsFor() - result.getGoalsAgainst());
                resultMap.put("place", result.getPlace());
                results.add(resultMap);
            });
            
            // Распределяем пользователей по группам
            Map<Integer, List<Long>> groups = groupingService.distributeToGroups(
                    results, 
                    request.getTopPlaces(), 
                    request.getGroupSize(), 
                    request.getTheme(), 
                    request.getSortType()
            );
            
            // Формируем ответ
            Map<String, Object> response = new HashMap<>();
            response.put("groups", groups);
            response.put("totalGroups", groups.size());
            response.put("totalPlayers", groups.values().stream().mapToInt(List::size).sum());
            response.put("theme", request.getTheme());
            response.put("sortType", request.getSortType());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Ошибка при создании групп",
                    "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Создает группы на основе результатов турнира (с параметрами в URL)
     * @param tournamentId ID турнира
     * @param topPlaces количество верхних мест для отбора
     * @param groupSize количество людей в одной группе
     * @param theme тематика для значения атрибута
     * @param sortType тип сортировки ("RandomSwizz" или "NoRandomSwizz")
     * @return Распределение пользователей по группам
     */
    @PostMapping("/create-groups")
    public ResponseEntity<?> createGroups(
            @RequestParam Long tournamentId,
            @RequestParam int topPlaces,
            @RequestParam int groupSize,
            @RequestParam String theme,
            @RequestParam String sortType) {
        
        try {
            // Получаем результаты турнира
            List<Map<String, Object>> results = new ArrayList<>();
            // Преобразуем PlayerResult в Map для упрощения работы с данными
            tournamentService.getTournamentResults(tournamentId).forEach(result -> {
                Map<String, Object> resultMap = new HashMap<>();
                resultMap.put("id", result.getId());
                resultMap.put("userId", result.getUser().getId());
                resultMap.put("username", result.getUser().getUsername());
                resultMap.put("points", result.getPoints());
                resultMap.put("wins", result.getWins());
                resultMap.put("losses", result.getLosses());
                resultMap.put("draws", result.getDraws());
                resultMap.put("goalsFor", result.getGoalsFor());
                resultMap.put("goalsAgainst", result.getGoalsAgainst());
                resultMap.put("goalDifference", result.getGoalsFor() - result.getGoalsAgainst());
                resultMap.put("place", result.getPlace());
                results.add(resultMap);
            });
            
            // Распределяем пользователей по группам
            Map<Integer, List<Long>> groups = groupingService.distributeToGroups(
                    results, topPlaces, groupSize, theme, sortType
            );
            
            // Формируем понятный ответ
            Map<String, Object> response = new HashMap<>();
            response.put("groups", groups);
            response.put("totalGroups", groups.size());
            response.put("totalPlayers", groups.values().stream().mapToInt(List::size).sum());
            response.put("theme", theme);
            response.put("sortType", sortType);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Ошибка при создании групп",
                    "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Получает группы по заданной тематике
     * @param theme тематика группировки
     * @return группы с пользователями
     */
    @GetMapping("/groups/{theme}")
    public ResponseEntity<?> getGroupsByTheme(@PathVariable String theme) {
        try {
            Map<String, List<Map<String, Object>>> groups = groupingService.getGroupsByTheme(theme);
            
            Map<String, Object> response = new HashMap<>();
            response.put("groups", groups);
            response.put("totalGroups", groups.size());
            response.put("theme", theme);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Ошибка при получении групп",
                    "message", e.getMessage()
            ));
        }
    }
}
