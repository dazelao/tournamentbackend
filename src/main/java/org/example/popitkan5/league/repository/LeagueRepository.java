package org.example.popitkan5.league.repository;

import org.example.popitkan5.league.model.League;
import org.example.popitkan5.league.model.LeagueStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeagueRepository extends JpaRepository<League, Long> {
    Optional<League> findByLeagueId(String leagueId);
    List<League> findByStatus(LeagueStatus status);
    Optional<League> findFirstByOrderByIdDesc();
}
