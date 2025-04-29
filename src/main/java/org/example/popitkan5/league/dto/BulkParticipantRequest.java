package org.example.popitkan5.league.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BulkParticipantRequest {
    
    @NotEmpty(message = "Список ID пользователей не может быть пустым")
    private List<Long> userIds;
    
    private String attribute; // Опциональный атрибут для фильтрации пользователей
}
