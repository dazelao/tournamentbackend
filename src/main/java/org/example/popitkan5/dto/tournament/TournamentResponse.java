package org.example.popitkan5.dto.tournament;

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
public class TournamentResponse {

    private Long id;

    private String name;

    private String status;
    
    private LocalDateTime startDate;
    
    private LocalDateTime endDate;
    
    private Integer maxParticipants;
    
    private Integer currentParticipants;
    
    private Long prizeFund;
    
    private Map<String, String> attributes = new HashMap<>();
}
