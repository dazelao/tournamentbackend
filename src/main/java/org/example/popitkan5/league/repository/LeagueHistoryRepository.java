package org.example.popitkan5.league.repository;

import org.example.popitkan5.league.model.LeagueHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeagueHistoryRepository extends JpaRepository<LeagueHistory, Long> {
    List<LeagueHistory> findByLeagueIdOrderByPositionAsc(Long leagueId);
    Optional<LeagueHistory> findByLeagueIdAndUserId(Long leagueId, Long userId);
    
    @Query("SELECT lh FROM LeagueHistory lh WHERE lh.leagueId = :leagueId ORDER BY lh.points DESC, (lh.goalsScored - lh.goalsConceded) DESC")
    List<LeagueHistory> findByLeagueIdOrderByPointsAndGoalDifference(@Param("leagueId") Long leagueId);
}
