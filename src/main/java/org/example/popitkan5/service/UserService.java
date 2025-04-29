package org.example.popitkan5.service;

import org.example.popitkan5.exception.UserNotFoundException;
import org.example.popitkan5.model.User;
import org.example.popitkan5.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, @Lazy PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Користувач не знайдений: " + username));

        List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
        );

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                authorities
        );
    }
    
    public User saveUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }
    
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
    
    public boolean existsByTelegram(String telegram) {
        return userRepository.existsByTelegram(telegram);
    }
    
    public boolean existsByEaId(String eaId) {
        return userRepository.existsByEaId(eaId);
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }
    
    /**
     * Получает список всех пользователей
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    /**
     * Получает список пользователей по ключу и значению атрибута
     */
    public List<User> getUsersByAttributeKeyAndValue(String key, String value) {
        return userRepository.findByAttributeKeyAndValue(key, value);
    }
    
    /**
     * Получает список пользователей по ключу атрибута
     */
    public List<User> getUsersByAttributeKey(String key) {
        return userRepository.findByAttributeKey(key);
    }
    
    /**
     * Получает список пользователей по значению атрибута
     */
    public List<User> getUsersByAttributeValue(String value) {
        return userRepository.findByAttributeValue(value);
    }
    
    /**
     * Получает список пользователей по нескольким ключам атрибутов
     */
    public List<User> getUsersByAttributeKeys(List<String> keys) {
        return userRepository.findByAttributeKeysIn(keys);
    }
    
    /**
     * Добавляет или обновляет атрибут пользователя
     */
    @Transactional
    public User addOrUpdateUserAttribute(Long userId, String key, String value) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с ID " + userId + " не найден"));
        
        Map<String, String> attributes = user.getAttributes();
        if (attributes == null) {
            attributes = new HashMap<>();
            user.setAttributes(attributes);
        }
        
        attributes.put(key, value);
        return userRepository.save(user);
    }
    
    /**
     * Удаляет атрибут пользователя
     */
    @Transactional
    public User removeUserAttribute(Long userId, String key) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с ID " + userId + " не найден"));
        
        Map<String, String> attributes = user.getAttributes();
        if (attributes != null) {
            attributes.remove(key);
            user.setAttributes(attributes);
        }
        
        return userRepository.save(user);
    }
    
    /**
     * Устанавливает все атрибуты пользователя
     */
    @Transactional
    public User setUserAttributes(Long userId, Map<String, String> attributes) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с ID " + userId + " не найден"));
        
        user.setAttributes(attributes);
        return userRepository.save(user);
    }
    
    /**
     * Очищает все атрибуты пользователя
     */
    @Transactional
    public User clearUserAttributes(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с ID " + userId + " не найден"));
        
        user.setAttributes(new HashMap<>());
        return userRepository.save(user);
    }
    
    /**
     * Массовое обновление атрибутов для выбранных пользователей
     * @param userIds список ID пользователей
     * @param attributes мапа атрибутов {ключ: значение}
     * @param merge режим слияния (true - добавить к существующим, false - заменить все)
     * @return количество обновленных пользователей
     */
    @Transactional
    public int bulkUpdateAttributes(List<Long> userIds, Map<String, String> attributes, boolean merge) {
        int updatedCount = 0;
        
        for (Long userId : userIds) {
            try {
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new UserNotFoundException("Пользователь с ID " + userId + " не найден"));
                
                Map<String, String> currentAttributes = user.getAttributes();
                if (currentAttributes == null) {
                    currentAttributes = new HashMap<>();
                    user.setAttributes(currentAttributes);
                }
                
                if (merge) {
                    // Режим слияния - добавляем атрибуты к существующим
                    currentAttributes.putAll(attributes);
                } else {
                    // Режим замены - заменяем все атрибуты
                    user.setAttributes(new HashMap<>(attributes));
                }
                
                userRepository.save(user);
                updatedCount++;
            } catch (UserNotFoundException e) {
                // Пропускаем ненайденных пользователей
                continue;
            }
        }
        
        return updatedCount;
    }
    
    /**
     * Получение пользователей по фильтру
     * @param usernamePattern шаблон имени пользователя
     * @param role роль
     * @param hasAttributeKeys список ключей атрибутов, которые должны присутствовать
     * @param hasAttributes атрибуты, которые должны присутствовать с определенными значениями
     * @return список пользователей, удовлетворяющих фильтру
     */
    public List<User> getUsersByFilter(String usernamePattern, String role, List<String> hasAttributeKeys, Map<String, String> hasAttributes) {
        List<User> users = getAllUsers();
        
        // Фильтрация по шаблону имени пользователя
        if (usernamePattern != null && !usernamePattern.isEmpty()) {
            users = users.stream()
                .filter(user -> user.getUsername().toLowerCase().contains(usernamePattern.toLowerCase()))
                .collect(Collectors.toList());
        }
        
        // Фильтрация по роли
        if (role != null && !role.isEmpty()) {
            users = users.stream()
                .filter(user -> user.getRole().name().equals(role))
                .collect(Collectors.toList());
        }
        
        // Фильтрация по наличию ключей атрибутов
        if (hasAttributeKeys != null && !hasAttributeKeys.isEmpty()) {
            users = users.stream()
                .filter(user -> {
                    Map<String, String> userAttributes = user.getAttributes();
                    if (userAttributes == null) {
                        return false;
                    }
                    return hasAttributeKeys.stream()
                        .allMatch(key -> userAttributes.containsKey(key));
                })
                .collect(Collectors.toList());
        }
        
        // Фильтрация по наличию атрибутов с определенными значениями
        if (hasAttributes != null && !hasAttributes.isEmpty()) {
            users = users.stream()
                .filter(user -> {
                    Map<String, String> userAttributes = user.getAttributes();
                    if (userAttributes == null) {
                        return false;
                    }
                    return hasAttributes.entrySet().stream()
                        .allMatch(entry -> entry.getValue().equals(userAttributes.get(entry.getKey())));
                })
                .collect(Collectors.toList());
        }
        
        return users;
    }
    
    /**
     * Массовое удаление атрибутов
     * @param userIds список ID пользователей
     * @param attributeKeys список ключей атрибутов для удаления
     * @return количество обновленных пользователей
     */
    @Transactional
    public int bulkDeleteAttributes(List<Long> userIds, List<String> attributeKeys) {
        int updatedCount = 0;
        
        for (Long userId : userIds) {
            try {
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new UserNotFoundException("Пользователь с ID " + userId + " не найден"));
                
                Map<String, String> attributes = user.getAttributes();
                if (attributes != null) {
                    boolean modified = false;
                    for (String key : attributeKeys) {
                        if (attributes.containsKey(key)) {
                            attributes.remove(key);
                            modified = true;
                        }
                    }
                    
                    if (modified) {
                        userRepository.save(user);
                        updatedCount++;
                    }
                }
            } catch (UserNotFoundException e) {
                // Пропускаем ненайденных пользователей
                continue;
            }
        }
        
        return updatedCount;
    }
    
    /**
     * Получение пользователей по частичному совпадению значения атрибута
     * @param valueFragment фрагмент значения атрибута
     * @return список пользователей
     */
    public List<Map<String, Object>> getUsersByAttributeValueContaining(String valueFragment) {
        List<User> allUsers = userRepository.findAll();
        
        // Фильтруем пользователей с атрибутами, содержащими фрагмент
        List<User> filteredUsers = allUsers.stream()
            .filter(user -> {
                Map<String, String> attributes = user.getAttributes();
                if (attributes == null || attributes.isEmpty()) {
                    return false;
                }
                // Проверяем, есть ли хотя бы одно значение, содержащее фрагмент
                return attributes.values().stream()
                    .anyMatch(value -> value != null && value.contains(valueFragment));
            })
            .collect(Collectors.toList());
        
        // Преобразуем в формат Map для удобства использования
        return filteredUsers.stream()
            .map(user -> {
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("id", user.getId());
                userMap.put("username", user.getUsername());
                userMap.put("role", user.getRole().name());
                userMap.put("attributes", user.getAttributes());
                return userMap;
            })
            .collect(Collectors.toList());
    }
}
