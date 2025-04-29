package org.example.popitkan5.patton;

import org.example.popitkan5.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class GroupingService {

    private final UserService userService;

    @Autowired
    public GroupingService(UserService userService) {
        this.userService = userService;
    }

    /**
     * Распределяет пользователей по группам
     * @param results список результатов игроков (с полями userId и place)
     * @param topPlaces количество верхних мест для отбора
     * @param groupSize количество людей в одной группе
     * @param theme тематика для значения атрибута
     * @param sortType тип сортировки ("RandomSwizz" или "NoRandomSwizz")
     * @return Map, где ключ - номер группы, значение - список ID пользователей в группе
     */
    public Map<Integer, List<Long>> distributeToGroups(
            List<Map<String, Object>> results, 
            int topPlaces, 
            int groupSize, 
            String theme,
            String sortType) {
        
        // Фильтруем результаты по топ местам
        List<Map<String, Object>> filteredResults = results.stream()
                .filter(r -> ((Integer)r.get("place")) <= topPlaces)
                .collect(Collectors.toList());
        
        Map<Integer, List<Long>> groups = new HashMap<>();
        
        if ("RandomSwizz".equals(sortType)) {
            groups = distributeWithRandomSwizz(filteredResults, groupSize);
        } else if ("NoRandomSwizz".equals(sortType)) {
            groups = distributeWithNoRandomSwizz(filteredResults, groupSize);
        }
        
        // Установка атрибутов пользователям
        setUserAttributes(groups, theme);
        
        return groups;
    }
    
    /**
     * Распределение с случайной сортировкой
     */
    private Map<Integer, List<Long>> distributeWithRandomSwizz(
            List<Map<String, Object>> results, 
            int groupSize) {
        
        // Копируем и перемешиваем случайно
        List<Map<String, Object>> shuffledResults = new ArrayList<>(results);
        Collections.shuffle(shuffledResults);
        
        Map<Integer, List<Long>> groups = new HashMap<>();
        int totalPlayers = shuffledResults.size();
        
        // Рассчитываем количество групп и размеры групп
        int[] groupInfo = calculateGroupSizesRandom(totalPlayers, groupSize);
        int numGroups = groupInfo[0];
        
        // Инициализируем группы
        for (int i = 1; i <= numGroups; i++) {
            groups.put(i, new ArrayList<>());
        }
        
        // Распределяем пользователей по группам
        int playerIndex = 0;
        for (int i = 0; i < numGroups; i++) {
            int currentGroupSize = (i < numGroups - 2) ? 
                    groupSize : 
                    (i == numGroups - 2 ? groupInfo[1] : groupInfo[2]);
            
            for (int j = 0; j < currentGroupSize && playerIndex < totalPlayers; j++) {
                Long userId = ((Number)shuffledResults.get(playerIndex).get("userId")).longValue();
                groups.get(i + 1).add(userId);
                playerIndex++;
            }
        }
        
        return groups;
    }
    
    /**
     * Распределение без случайной сортировки (по местам в "змейку")
     */
    private Map<Integer, List<Long>> distributeWithNoRandomSwizz(
            List<Map<String, Object>> results, 
            int groupSize) {
        
        // Сортируем по месту
        results.sort(Comparator.comparingInt(r -> (Integer)r.get("place")));
        
        int totalPlayers = results.size();
        int numGroups = (int) Math.ceil((double) totalPlayers / groupSize);
        
        Map<Integer, List<Long>> groups = new HashMap<>();
        
        // Инициализируем группы
        for (int i = 1; i <= numGroups; i++) {
            groups.put(i, new ArrayList<>());
        }
        
        // Распределяем пользователей по группам в порядке "змейки"
        for (int i = 0; i < results.size(); i++) {
            int groupIndex = (i % numGroups) + 1;
            Long userId = ((Number)results.get(i).get("userId")).longValue();
            groups.get(groupIndex).add(userId);
        }
        
        return groups;
    }
    
    /**
     * Расчет количества групп и размеров последних групп для случайного распределения
     * @return массив из 3 чисел: [количество групп, размер предпоследней группы, размер последней группы]
     */
    private int[] calculateGroupSizesRandom(int totalPlayers, int groupSize) {
        int[] result = new int[3];
        
        // Если делится ровно - все просто
        if (totalPlayers % groupSize == 0) {
            result[0] = totalPlayers / groupSize; // количество групп
            result[1] = groupSize; // размер предпоследней группы
            result[2] = groupSize; // размер последней группы
            return result;
        }
        
        // Находим количество полных групп и остаток
        int fullGroups = totalPlayers / groupSize;
        int remainder = totalPlayers % groupSize;
        
        // Вычисляем коэффициент остатка
        double remainderRatio = (double) remainder / groupSize;
        
        if (remainderRatio >= 0.8) {
            // Создаем одну дополнительную группу с остатком
            result[0] = fullGroups + 1;
            result[1] = groupSize;
            result[2] = remainder;
        } else {
            // Если остаток маленький, уменьшаем количество групп и распределяем
            int adjustedGroups = Math.max(1, fullGroups - 1);
            result[0] = adjustedGroups;
            
            // Распределяем остаток между последними двумя группами
            int redistributedSize = totalPlayers / adjustedGroups;
            int newRemainder = totalPlayers % adjustedGroups;
            
            if (adjustedGroups == 1) {
                // Если осталась только 1 группа
                result[1] = totalPlayers;
                result[2] = 0;
            } else {
                // Если 2+ группы, делим остаток
                result[1] = redistributedSize + (newRemainder + 1) / 2;
                result[2] = redistributedSize + newRemainder / 2;
            }
        }
        
        return result;
    }
    
    /**
     * Установка атрибутов пользователям по группам
     */
    private void setUserAttributes(Map<Integer, List<Long>> groups, String theme) {
        int totalGroups = groups.size();
        
        for (Map.Entry<Integer, List<Long>> entry : groups.entrySet()) {
            Integer groupNumber = entry.getKey();
            List<Long> userIds = entry.getValue();
            
            String attributeValue = "group" + groupNumber + "_" + totalGroups + "_" + theme;
            
            // Используем сервис для массовой установки атрибутов
            userService.bulkUpdateAttributes(
                userIds, 
                Collections.singletonMap("massadd", attributeValue), 
                true
            );
        }
    }
    
    /**
     * Получает информацию о группах по атрибутам пользователей
     * @param theme тематика, по которой производилась группировка
     * @return Map, где ключ - номер группы, значение - список пользователей
     */
    public Map<String, List<Map<String, Object>>> getGroupsByTheme(String theme) {
        // Получаем пользователей, у которых есть атрибут massadd и значение содержит theme
        List<Map<String, Object>> users = userService.getUsersByAttributeValueContaining(theme);
        
        Map<String, List<Map<String, Object>>> result = new HashMap<>();
        
        for (Map<String, Object> user : users) {
            Map<String, String> attributes = (Map<String, String>) user.get("attributes");
            if (attributes != null && attributes.containsKey("massadd")) {
                String groupValue = attributes.get("massadd");
                if (groupValue != null && groupValue.contains(theme)) {
                    // Добавляем пользователя в соответствующую группу
                    if (!result.containsKey(groupValue)) {
                        result.put(groupValue, new ArrayList<>());
                    }
                    result.get(groupValue).add(user);
                }
            }
        }
        
        return result;
    }
}
