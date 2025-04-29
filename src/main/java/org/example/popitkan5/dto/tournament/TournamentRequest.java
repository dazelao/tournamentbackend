package org.example.popitkan5.dto.tournament;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TournamentRequest {

    @NotBlank(message = "Название турнира обязательно")
    @Size(min = 3, max = 100, message = "Название турнира должно содержать от 3 до 100 символов")
    private String name;

    private String status = "planned";
    
    private LocalDateTime startDate;
    
    private LocalDateTime endDate;
    
    private Integer maxParticipants;
    
    private Long prizeFund;
    
    private Map<String, String> attributes = new HashMap<>();
}
