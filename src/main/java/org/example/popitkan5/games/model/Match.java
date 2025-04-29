package org.example.popitkan5.games.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "matches")
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id_1", nullable = false)
    private Long userId1;

    @Column(name = "user_id_2")
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

    @Column(name = "tournament_id")
    private Long tournamentId;
    
    @Column(name = "round_number")
    private Integer roundNumber;

    @Column(name = "winner_id")
    private Long winnerId;

    @Column(name = "loser_id")
    private Long loserId;

    @Column(name = "draw_user_1")
    private Long drawUser1;

    @Column(name = "draw_user_2")
    private Long drawUser2;

    // Автоматически устанавливаем дату создания
    @PrePersist
    protected void onCreate() {
        this.createdDate = LocalDateTime.now();
    }
}
