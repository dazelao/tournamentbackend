package org.example.popitkan5.service;

import org.example.popitkan5.dto.AuthRequest;
import org.example.popitkan5.dto.AuthResponse;
import org.example.popitkan5.dto.RegisterRequest;
import org.example.popitkan5.dto.UpdateUserRequest;
import org.example.popitkan5.model.User;
import org.example.popitkan5.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    @Autowired
    public AuthService(UserService userService, JwtTokenProvider jwtTokenProvider, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authenticationManager = authenticationManager;
    }

    public AuthResponse register(RegisterRequest request) {
        // Проверка на существование пользователя с таким именем
        if (userService.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Користувач з таким ім'ям вже існує");
        }

        // Проверка на существование пользователя с таким телеграмом (если указан)
        if (request.getTelegram() != null && !request.getTelegram().isBlank() && 
            userService.existsByTelegram(request.getTelegram())) {
            throw new RuntimeException("Користувач з таким телеграмом вже існує");
        }

        // Проверка на существование пользователя с таким EA ID (если указан)
        if (request.getEaId() != null && !request.getEaId().isBlank() && 
            userService.existsByEaId(request.getEaId())) {
            throw new RuntimeException("Користувач з таким EA ID вже існує");
        }

        // Создание пользователя
        User user = new User();
        user.setUsername(request.getUsername());
        user.setTelegram(request.getTelegram());
        user.setEaId(request.getEaId());
        user.setPassword(request.getPassword()); // Шифрование произойдет в сервисе
        user.setRole(request.getRole());
        user.setAttributes(request.getAttributes());

        // Сохранение пользователя в базу данных
        User savedUser = userService.saveUser(user);

        // Создание JWT токена
        UserDetails userDetails = userService.loadUserByUsername(savedUser.getUsername());
        String token = jwtTokenProvider.generateToken(userDetails, savedUser);

        return AuthResponse.builder()
                .id(savedUser.getId())
                .token(token)
                .username(savedUser.getUsername())
                .role(savedUser.getRole())
                .build();
    }

    public AuthResponse authenticate(AuthRequest request) {
        // Аутентификация пользователя
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        // Получение пользователя из базы данных
        User user = userService.getUserByUsername(request.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Користувач не знайдений"));

        // Создание JWT токена
        UserDetails userDetails = userService.loadUserByUsername(user.getUsername());
        String token = jwtTokenProvider.generateToken(userDetails, user);

        return AuthResponse.builder()
                .id(user.getId())
                .token(token)
                .username(user.getUsername())
                .role(user.getRole())
                .build();
    }

    public User getCurrentUser(String username) {
        return userService.getUserByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Користувач не знайдений"));
    }

    @Transactional
    public User updateUser(String username, UpdateUserRequest request) {
        User user = userService.getUserByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Користувач не знайдений"));

        // Проверка на существование пользователя с таким телеграмом
        if (request.getTelegram() != null && !request.getTelegram().isBlank()) {
            if (!request.getTelegram().equals(user.getTelegram()) && 
                userService.existsByTelegram(request.getTelegram())) {
                throw new RuntimeException("Користувач з таким телеграмом вже існує");
            }
            user.setTelegram(request.getTelegram());
        }

        // Проверка на существование пользователя с таким EA ID
        if (request.getEaId() != null && !request.getEaId().isBlank()) {
            if (!request.getEaId().equals(user.getEaId()) && 
                userService.existsByEaId(request.getEaId())) {
                throw new RuntimeException("Користувач з таким EA ID вже існує");
            }
            user.setEaId(request.getEaId());
        }

        return userService.saveUser(user);
    }
}