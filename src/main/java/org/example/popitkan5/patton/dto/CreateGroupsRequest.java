package org.example.popitkan5.patton.dto;

public class CreateGroupsRequest {
    private Long tournamentId;
    private int topPlaces;
    private int groupSize;
    private String theme;
    private String sortType;
    
    // Геттеры и сеттеры
    public Long getTournamentId() {
        return tournamentId;
    }
    
    public void setTournamentId(Long tournamentId) {
        this.tournamentId = tournamentId;
    }
    
    public int getTopPlaces() {
        return topPlaces;
    }
    
    public void setTopPlaces(int topPlaces) {
        this.topPlaces = topPlaces;
    }
    
    public int getGroupSize() {
        return groupSize;
    }
    
    public void setGroupSize(int groupSize) {
        this.groupSize = groupSize;
    }
    
    public String getTheme() {
        return theme;
    }
    
    public void setTheme(String theme) {
        this.theme = theme;
    }
    
    public String getSortType() {
        return sortType;
    }
    
    public void setSortType(String sortType) {
        this.sortType = sortType;
    }
}
