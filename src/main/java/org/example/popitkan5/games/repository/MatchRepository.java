package org.example.popitkan5.games.repository;

import org.example.popitkan5.games.model.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {
    
    List<Match> findByTournamentId(Long tournamentId);
    
    List<Match> findByUserId1OrUserId2(Long userId1, Long userId2);
    
    List<Match> findByTournamentIdAndRoundNumber(Long tournamentId, Integer roundNumber);
}
