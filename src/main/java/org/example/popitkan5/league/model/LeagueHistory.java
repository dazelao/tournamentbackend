package org.example.popitkan5.league.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "league_history",
       uniqueConstraints = @UniqueConstraint(columnNames = {"league_id", "user_id"}))
public class LeagueHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "league_id", nullable = false)
    private Long leagueId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "matches_played", nullable = false)
    private Integer matchesPlayed = 0;

    @Column(name = "wins", nullable = false)
    private Integer wins = 0;

    @Column(name = "losses", nullable = false)
    private Integer losses = 0;

    @Column(name = "draws", nullable = false)
    private Integer draws = 0;

    @Column(name = "goals_scored", nullable = false)
    private Integer goalsScored = 0;

    @Column(name = "goals_conceded", nullable = false)
    private Integer goalsConceded = 0;

    @Column(name = "points", nullable = false)
    private Integer points = 0;

    @Column(name = "position", nullable = false)
    private Integer position;
}
