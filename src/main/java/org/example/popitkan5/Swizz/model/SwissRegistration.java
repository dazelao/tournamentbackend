package org.example.popitkan5.Swizz.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.popitkan5.model.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "swiss_registrations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SwissRegistration {
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
    private LocalDateTime registrationTime;

    @Column(nullable = false)
    private boolean active;

    public SwissRegistration(SwissTournament tournament, User user) {
        this.tournament = tournament;
        this.user = user;
        this.registrationTime = LocalDateTime.now();
        this.active = true;
    }
}
