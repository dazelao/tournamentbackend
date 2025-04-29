package org.example.popitkan5.Swizz.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.popitkan5.model.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "swiss_player_results")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id", nullable = false)
    private SwissTournament tournament;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Integer round;

    @Column(nullable = false)
    private Integer points = 0;

    @Column(nullable = false)
    private Integer wins = 0;

    @Column(nullable = false)
    private Integer losses = 0;

    @Column(nullable = false)
    private Integer draws = 0;

    @Column(nullable = false)
    private Integer goalsFor = 0;

    @Column(nullable = false)
    private Integer goalsAgainst = 0;

    @Column
    private Integer place;

    @Column(nullable = false)
    private LocalDateTime lastUpdated;

    public PlayerResult(SwissTournament tournament, User user, Integer round) {
        this.tournament = tournament;
        this.user = user;
        this.round = round;
        this.points = 0;
        this.wins = 0;
        this.losses = 0;
        this.draws = 0;
        this.goalsFor = 0;
        this.goalsAgainst = 0;
        this.lastUpdated = LocalDateTime.now();
    }

    public void addMatchResult(int goalsFor, int goalsAgainst) {
        this.goalsFor += goalsFor;
        this.goalsAgainst += goalsAgainst;

        if (goalsFor > goalsAgainst) {
            this.points += 3;
            this.wins += 1;
        } else if (goalsFor < goalsAgainst) {
            this.losses += 1;
        } else {
            this.points += 1;
            this.draws += 1;
        }

        this.lastUpdated = LocalDateTime.now();
    }

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        lastUpdated = LocalDateTime.now();
    }
}
