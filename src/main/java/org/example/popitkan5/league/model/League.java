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
@Table(name = "leagues")
public class League {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String leagueId; // ID в виде L(номер по порядку)

    @Column(nullable = false)
    private String name;

    @Column(name = "max_participants", nullable = false)
    private Integer maxParticipants;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeagueStatus status = LeagueStatus.DRAFT;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "modified_date")
    private LocalDateTime modifiedDate;

    // Атрибуты для победителей, проигравших и сохранивших прописку
    @Column(name = "winner_attribute")
    private String winnerAttribute; // Название_турнира_win

    @Column(name = "loser_attribute")
    private String loserAttribute; // Название_турнира_lose

    @Column(name = "save_attribute")
    private String saveAttribute; // Название_турнира_save

    // Количество участников для каждого атрибута
    @Column(name = "winner_count")
    private Integer winnerCount;

    @Column(name = "loser_count")
    private Integer loserCount;

    @Column(name = "save_count")
    private Integer saveCount;

    // Автоматически устанавливаем дату создания и генерируем leagueId
    @PrePersist
    protected void onCreate() {
        this.createdDate = LocalDateTime.now();
        this.modifiedDate = LocalDateTime.now();
        
        // Логика генерации leagueId будет реализована в сервисе
        if (this.leagueId == null) {
            this.leagueId = "L" + this.id; // Временное решение
        }
    }

    // При изменении автоматически устанавливаем дату изменения
    @PreUpdate
    protected void onUpdate() {
        this.modifiedDate = LocalDateTime.now();
    }
}
