package org.example.popitkan5.Swizz.repository;

import org.example.popitkan5.Swizz.model.SwissTournament;
import org.example.popitkan5.Swizz.model.SwissTournamentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SwissTournamentRepository extends JpaRepository<SwissTournament, Long> {
    
    List<SwissTournament> findByStatus(SwissTournamentStatus status);
    
    @Query("SELECT t FROM SwissTournament t WHERE t.status = 'REGISTRATION_OPEN' AND t.startDate <= CURRENT_TIMESTAMP")
    List<SwissTournament> findActiveRegistrations();
    
    @Query("SELECT t FROM SwissTournament t WHERE t.status = 'IN_PROGRESS'")
    List<SwissTournament> findActiveTournaments();
    
    @Query("SELECT t FROM SwissTournament t WHERE t.status = 'COMPLETED' ORDER BY t.endDate DESC")
    List<SwissTournament> findRecentlyCompletedTournaments();
}
