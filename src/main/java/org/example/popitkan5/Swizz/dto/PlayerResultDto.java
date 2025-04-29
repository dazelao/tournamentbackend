package org.example.popitkan5.Swizz.dto;

import lombok.Data;
import org.example.popitkan5.Swizz.model.PlayerResult;

@Data
public class PlayerResultDto {
    private Long id;
    private Long userId;
    private String username;
    private Integer points;
    private Integer wins;
    private Integer losses;
    private Integer draws;
    private Integer goalsFor;
    private Integer goalsAgainst;
    private Integer goalDifference;
    private Integer place;

    public static PlayerResultDto fromEntity(PlayerResult result) {
        PlayerResultDto dto = new PlayerResultDto();
        dto.setId(result.getId());
        dto.setUserId(result.getUser().getId());
        dto.setUsername(result.getUser().getUsername());
        dto.setPoints(result.getPoints());
        dto.setWins(result.getWins());
        dto.setLosses(result.getLosses());
        dto.setDraws(result.getDraws());
        dto.setGoalsFor(result.getGoalsFor());
        dto.setGoalsAgainst(result.getGoalsAgainst());
        dto.setGoalDifference(result.getGoalsFor() - result.getGoalsAgainst());
        dto.setPlace(result.getPlace());
        
        return dto;
    }
}
