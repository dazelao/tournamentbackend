package org.example.popitkan5.Swizz.repository;

import org.example.popitkan5.Swizz.model.MatchStatus;
import org.example.popitkan5.Swizz.model.SwissMatch;
import org.example.popitkan5.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SwissMatchRepository extends JpaRepository<SwissMatch, Long> {
    
    @Query("SELECT m FROM SwissMatch m LEFT JOIN FETCH m.player1 LEFT JOIN FETCH m.player2 " +
           "WHERE m.tournament.id = :tournamentId AND m.round = :round")
    List<SwissMatch> findByTournamentIdAndRound(@Param("tournamentId") Long tournamentId, @Param("round") Integer round);
    
    List<SwissMatch> findByTournamentIdAndStatus(Long tournamentId, MatchStatus status);
    
    @Query("SELECT m FROM SwissMatch m WHERE m.tournament.id = :tournamentId AND m.round = :round " +
           "AND (m.player1.id = :playerId OR m.player2.id = :playerId)")
    List<SwissMatch> findPlayerMatchesInRound(@Param("tournamentId") Long tournamentId, 
                                             @Param("round") Integer round, 
                                             @Param("playerId") Long playerId);
    
    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM SwissMatch m " +
           "WHERE m.tournament.id = :tournamentId AND m.status = 'COMPLETED' " +
           "AND ((m.player1.id = :player1Id AND m.player2.id = :player2Id) " +
           "OR (m.player1.id = :player2Id AND m.player2.id = :player1Id))")
    boolean havePlayersPlayed(@Param("tournamentId") Long tournamentId, 
                             @Param("player1Id") Long player1Id, 
                             @Param("player2Id") Long player2Id);
    
    @Query("SELECT COUNT(m) FROM SwissMatch m WHERE m.tournament.id = :tournamentId " +
           "AND m.round = :round AND m.status = 'COMPLETED'")
    int countCompletedMatchesInRound(@Param("tournamentId") Long tournamentId, 
                                    @Param("round") Integer round);
    
    @Query("SELECT COUNT(m) FROM SwissMatch m WHERE m.tournament.id = :tournamentId " +
           "AND m.round = :round")
    int countTotalMatchesInRound(@Param("tournamentId") Long tournamentId, 
                                @Param("round") Integer round);
}
