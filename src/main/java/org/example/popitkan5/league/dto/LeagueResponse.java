package org.example.popitkan5.league.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.popitkan5.league.model.LeagueStatus;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeagueResponse {
    private Long id;
    private String leagueId;
    private String name;
    private Integer maxParticipants;
    private LeagueStatus status;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private Integer currentParticipants;
    
    // Информация об атрибутах
    private String winnerAttribute;
    private String loserAttribute;
    private String saveAttribute;
    private Integer winnerCount;
    private Integer loserCount;
    private Integer saveCount;
}
