package org.example.popitkan5.league.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "league_matches")
public class LeagueMatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id_1", nullable = false)
    private Long userId1;

    @Column(name = "user_id_2", nullable = false)
    private Long userId2;

    @Column(name = "goals_user_1")
    private Integer goalsUser1;

    @Column(name = "goals_user_2")
    private Integer goalsUser2;

    @Column(name = "result_user_1")
    private String resultUser1;

    @Column(name = "result_user_2")
    private String resultUser2;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "modified_date")
    private LocalDateTime modifiedDate;

    @Column(name = "league_id", nullable = false)
    private Long leagueId;

    @Column(name = "round_number")
    private Integer roundNumber;

    @Column(name = "winner_id")
    private Long winnerId;

    @Column(name = "loser_id")
    private Long loserId;

    @Column(name = "is_draw")
    private Boolean isDraw = false;

    // Автоматически устанавливаем дату создания
    @PrePersist
    protected void onCreate() {
        this.createdDate = LocalDateTime.now();
    }

    // При изменении автоматически устанавливаем дату изменения
    @PreUpdate
    protected void onUpdate() {
        this.modifiedDate = LocalDateTime.now();
    }
}
