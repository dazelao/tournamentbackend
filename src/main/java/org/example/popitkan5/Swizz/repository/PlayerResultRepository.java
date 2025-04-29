package org.example.popitkan5.Swizz.repository;

import org.example.popitkan5.Swizz.model.PlayerResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerResultRepository extends JpaRepository<PlayerResult, Long> {
    
    List<PlayerResult> findByTournamentIdOrderByPointsDescGoalsForDesc(Long tournamentId);
    
    Optional<PlayerResult> findByTournamentIdAndUserId(Long tournamentId, Long userId);
    
    @Query("SELECT pr FROM PlayerResult pr JOIN FETCH pr.user WHERE pr.tournament.id = :tournamentId ORDER BY " +
           "pr.points DESC, (pr.goalsFor - pr.goalsAgainst) DESC, pr.goalsFor DESC")
    List<PlayerResult> findTournamentResultsSorted(@Param("tournamentId") Long tournamentId);
}
