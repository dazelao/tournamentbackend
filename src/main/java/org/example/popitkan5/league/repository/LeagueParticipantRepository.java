package org.example.popitkan5.league.repository;

import org.example.popitkan5.league.model.LeagueParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeagueParticipantRepository extends JpaRepository<LeagueParticipant, Long> {
    List<LeagueParticipant> findByLeagueId(Long leagueId);
    Optional<LeagueParticipant> findByLeagueIdAndUserId(Long leagueId, Long userId);
    void deleteByLeagueIdAndUserId(Long leagueId, Long userId);
    int countByLeagueId(Long leagueId);
}
