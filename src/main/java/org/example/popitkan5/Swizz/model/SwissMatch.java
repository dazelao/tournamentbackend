package org.example.popitkan5.Swizz.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.popitkan5.model.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "swiss_matches")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SwissMatch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id", nullable = false)
    private SwissTournament tournament;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player1_id", nullable = false)
    private User player1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player2_id")
    private User player2;

    @Column(nullable = false)
    private Integer round;

    @Column
    private Integer player1Score;

    @Column
    private Integer player2Score;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchStatus status;

    @Column
    private LocalDateTime scheduledTime;

    @Column
    private LocalDateTime completedTime;

    @Column
    private String matchUrl;

    public SwissMatch(SwissTournament tournament, User player1, User player2, Integer round, MatchStatus status) {
        this.tournament = tournament;
        this.player1 = player1;
        this.player2 = player2;
        this.round = round;
        this.status = status;
        this.scheduledTime = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (status == null) {
            status = MatchStatus.SCHEDULED;
        }
    }
}
