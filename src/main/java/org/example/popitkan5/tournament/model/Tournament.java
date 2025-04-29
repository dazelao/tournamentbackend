package org.example.popitkan5.tournament.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tournaments")
public class Tournament {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TournamentStatus status = TournamentStatus.DRAFT;

    @Column(name = "max_participants", nullable = false)
    private Integer maxParticipants;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "modified_at")
    private LocalDateTime modifiedAt;

    @Column(name = "current_round")
    private Integer currentRound = 0;

    // При создании или изменении автоматически устанавливаем дату изменения
    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        this.modifiedAt = LocalDateTime.now();
    }
}
