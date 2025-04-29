package org.example.popitkan5.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

/**
 * Простой контроллер для тестирования API
 */
@RestController
@RequestMapping("/api/simple")
public class SimpleApiController {

    @GetMapping
    public ResponseEntity<Map<String, String>> hello() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Привет от DonChamps API!");
        return ResponseEntity.ok(response);
    }
}
