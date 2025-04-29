package org.example.popitkan5.tournament.repository;

import org.example.popitkan5.tournament.model.Tournament;
import org.example.popitkan5.tournament.model.TournamentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TournamentRepository extends JpaRepository<Tournament, Long> {
    List<Tournament> findByStatus(TournamentStatus status);
}
