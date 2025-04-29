package org.example.popitkan5.controller;

import lombok.RequiredArgsConstructor;
import org.example.popitkan5.dto.AttributeRequest;
import org.example.popitkan5.exception.UserNotFoundException;
import org.example.popitkan5.model.User;
import org.example.popitkan5.repository.UserRepository;
import org.example.popitkan5.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Optional<User> user = userRepository.findByUsername(authentication.getName());
        
        return user.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<User>> getUsers() {
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/all")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        Optional<User> user = userRepository.findById(id);
        
        return user.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<User>> searchUsers(@RequestParam String query) {
        List<User> users = userRepository.findByUsernameContainingIgnoreCase(query);
        return ResponseEntity.ok(users);
    }

    @PutMapping("/attributes")
    public ResponseEntity<Map<String, String>> updateAttributes(@RequestBody Map<String, String> attributes) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Optional<User> userOpt = userRepository.findByUsername(authentication.getName());
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            Map<String, String> currentAttributes = user.getAttributes();
            
            // Обновление атрибутов
            if (currentAttributes == null) {
                currentAttributes = new HashMap<>();
            }
            
            currentAttributes.putAll(attributes);
            user.setAttributes(currentAttributes);
            userRepository.save(user);
            
            return ResponseEntity.ok(user.getAttributes());
        }
        
        return ResponseEntity.notFound().build();
    }
    
    // Новые методы для работы с атрибутами

    /**
     * Получение пользователей по ключу и значению атрибута
     */
    @GetMapping("/by-attribute")
    public ResponseEntity<List<User>> getUsersByAttribute(
            @RequestParam(required = false) String key,
            @RequestParam(required = false) String value) {
        
        List<User> users;
        if (key != null && value != null) {
            // Поиск по ключу и значению
            users = userService.getUsersByAttributeKeyAndValue(key, value);
        } else if (key != null) {
            // Поиск только по ключу
            users = userService.getUsersByAttributeKey(key);
        } else if (value != null) {
            // Поиск только по значению
            users = userService.getUsersByAttributeValue(value);
        } else {
            // Если ничего не указано, возвращаем всех пользователей
            users = userService.getAllUsers();
        }
        
        return ResponseEntity.ok(users);
    }
    
    /**
     * Получение пользователей по нескольким ключам атрибутов
     */
    @GetMapping("/by-attributes")
    public ResponseEntity<List<User>> getUsersByAttributes(@RequestParam List<String> keys) {
        List<User> users = userService.getUsersByAttributeKeys(keys);
        return ResponseEntity.ok(users);
    }
    
    /**
     * Добавление или обновление атрибута пользователя (для администраторов)
     */
    @PutMapping("/{userId}/attributes")
    public ResponseEntity<User> addOrUpdateUserAttribute(
            @PathVariable Long userId,
            @RequestBody AttributeRequest request) {
        
        try {
            User updatedUser = userService.addOrUpdateUserAttribute(userId, request.getKey(), request.getValue());
            return ResponseEntity.ok(updatedUser);
        } catch (UserNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Удаление атрибута пользователя (для администраторов)
     */
    @DeleteMapping("/{userId}/attributes/{key}")
    public ResponseEntity<User> removeUserAttribute(
            @PathVariable Long userId,
            @PathVariable String key) {
        
        try {
            User updatedUser = userService.removeUserAttribute(userId, key);
            return ResponseEntity.ok(updatedUser);
        } catch (UserNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Задание всех атрибутов пользователя (для администраторов)
     */
    @PutMapping("/{userId}/attributes/all")
    public ResponseEntity<User> setUserAttributes(
            @PathVariable Long userId,
            @RequestBody Map<String, String> attributes) {
        
        try {
            User updatedUser = userService.setUserAttributes(userId, attributes);
            return ResponseEntity.ok(updatedUser);
        } catch (UserNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Очистка всех атрибутов пользователя (для администраторов)
     */
    @DeleteMapping("/{userId}/attributes")
    public ResponseEntity<User> clearUserAttributes(@PathVariable Long userId) {
        try {
            User updatedUser = userService.clearUserAttributes(userId);
            return ResponseEntity.ok(updatedUser);
        } catch (UserNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Добавление или обновление своего атрибута (для текущего пользователя)
     */
    @PutMapping("/me/attributes")
    public ResponseEntity<User> addOrUpdateMyAttribute(@RequestBody AttributeRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Optional<User> userOpt = userRepository.findByUsername(authentication.getName());
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            User updatedUser = userService.addOrUpdateUserAttribute(user.getId(), request.getKey(), request.getValue());
            return ResponseEntity.ok(updatedUser);
        }
        
        return ResponseEntity.notFound().build();
    }
    
    /**
     * Удаление своего атрибута (для текущего пользователя)
     */
    @DeleteMapping("/me/attributes/{key}")
    public ResponseEntity<User> removeMyAttribute(@PathVariable String key) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Optional<User> userOpt = userRepository.findByUsername(authentication.getName());
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            User updatedUser = userService.removeUserAttribute(user.getId(), key);
            return ResponseEntity.ok(updatedUser);
        }
        
        return ResponseEntity.notFound().build();
    }
    
    /**
     * Массовое обновление атрибутов для выбранных пользователей
     */
    @PutMapping("/attributes/bulk")
    public ResponseEntity<?> bulkUpdateAttributes(@RequestBody Map<String, Object> request) {
        try {
            List<Integer> integerIds = (List<Integer>) request.get("userIds");
            List<Long> userIds = integerIds.stream().map(Integer::longValue).collect(Collectors.toList());
            
            @SuppressWarnings("unchecked")
            Map<String, String> attributes = (Map<String, String>) request.get("attributes");
            boolean merge = Boolean.TRUE.equals(request.getOrDefault("merge", true));
            
            if (userIds == null || attributes == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "userIds и attributes являются обязательными полями"
                ));
            }
            
            int updatedCount = userService.bulkUpdateAttributes(userIds, attributes, merge);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "updatedCount", updatedCount,
                "totalRequested", userIds.size(),
                "totalProcessed", updatedCount,
                "totalSkipped", userIds.size() - updatedCount
            ));
        } catch (ClassCastException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "error", "Ошибка формата данных",
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "Ошибка при обновлении атрибутов",
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Массовое обновление атрибутов по фильтру
     */
    @PutMapping("/attributes/filter")
    public ResponseEntity<?> bulkAttributesByFilter(@RequestBody Map<String, Object> request) {
        try {
            String usernamePattern = (String) request.getOrDefault("usernamePattern", "");
            String role = (String) request.getOrDefault("role", "");
            
            @SuppressWarnings("unchecked")
            List<String> hasAttributeKeys = (List<String>) request.getOrDefault("hasAttributeKeys", List.of());
            
            @SuppressWarnings("unchecked")
            Map<String, String> hasAttributes = (Map<String, String>) request.getOrDefault("hasAttributes", Map.of());
            
            @SuppressWarnings("unchecked")
            Map<String, String> attributes = (Map<String, String>) request.get("attributes");
            boolean merge = Boolean.TRUE.equals(request.getOrDefault("merge", true));
            
            if (attributes == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "attributes является обязательным полем"
                ));
            }
            
            // Получаем список пользователей по фильтру
            List<User> filteredUsers = userService.getUsersByFilter(
                usernamePattern, 
                role, 
                hasAttributeKeys, 
                hasAttributes
            );
            
            List<Long> userIds = filteredUsers.stream()
                .map(User::getId)
                .collect(Collectors.toList());
            
            int updatedCount = userService.bulkUpdateAttributes(userIds, attributes, merge);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "updatedCount", updatedCount,
                "totalRequested", userIds.size(),
                "totalProcessed", updatedCount,
                "totalSkipped", userIds.size() - updatedCount
            ));
        } catch (ClassCastException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "error", "Ошибка формата данных",
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "Ошибка при обновлении атрибутов по фильтру",
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Массовое удаление атрибутов
     */
    @DeleteMapping("/attributes/bulk")
    public ResponseEntity<?> bulkDeleteAttributes(@RequestBody Map<String, Object> request) {
        try {
            List<Integer> integerIds = (List<Integer>) request.get("userIds");
            List<Long> userIds = integerIds.stream().map(Integer::longValue).collect(Collectors.toList());
            
            @SuppressWarnings("unchecked")
            List<String> attributeKeys = (List<String>) request.get("attributeKeys");
            
            if (userIds == null || attributeKeys == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "userIds и attributeKeys являются обязательными полями"
                ));
            }
            
            int updatedCount = userService.bulkDeleteAttributes(userIds, attributeKeys);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "updatedCount", updatedCount,
                "totalRequested", userIds.size(),
                "totalProcessed", updatedCount,
                "totalSkipped", userIds.size() - updatedCount
            ));
        } catch (ClassCastException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "error", "Ошибка формата данных",
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "Ошибка при удалении атрибутов",
                "message", e.getMessage()
            ));
        }
    }
}
