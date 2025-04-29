package org.example.popitkan5.league.repository;

import org.example.popitkan5.league.model.LeagueMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeagueMatchRepository extends JpaRepository<LeagueMatch, Long> {
    List<LeagueMatch> findByLeagueId(Long leagueId);
    
    @Query("SELECT m FROM LeagueMatch m WHERE m.leagueId = :leagueId AND (m.userId1 = :userId OR m.userId2 = :userId)")
    List<LeagueMatch> findByLeagueIdAndUserId(@Param("leagueId") Long leagueId, @Param("userId") Long userId);
    
    @Query("SELECT m FROM LeagueMatch m WHERE m.leagueId = :leagueId AND ((m.userId1 = :userId1 AND m.userId2 = :userId2) OR (m.userId1 = :userId2 AND m.userId2 = :userId1))")
    List<LeagueMatch> findPersonalMatches(@Param("leagueId") Long leagueId, @Param("userId1") Long userId1, @Param("userId2") Long userId2);
    
    @Query("SELECT COUNT(m) FROM LeagueMatch m WHERE m.leagueId = :leagueId AND (m.winnerId IS NOT NULL OR m.isDraw = true)")
    int countCompletedMatchesByLeagueId(@Param("leagueId") Long leagueId);
    
    @Query("SELECT COUNT(m) FROM LeagueMatch m WHERE m.leagueId = :leagueId")
    int countTotalMatchesByLeagueId(@Param("leagueId") Long leagueId);

    @Query("SELECT m FROM LeagueMatch m WHERE m.userId1 = :userId OR m.userId2 = :userId ORDER BY m.createdDate DESC")
    List<LeagueMatch> findByUserIdInAllLeagues(@Param("userId") Long userId);
}
