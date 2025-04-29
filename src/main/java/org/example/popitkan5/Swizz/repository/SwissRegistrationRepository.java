package org.example.popitkan5.Swizz.repository;

import org.example.popitkan5.Swizz.model.SwissRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SwissRegistrationRepository extends JpaRepository<SwissRegistration, Long> {
    
    List<SwissRegistration> findByTournamentIdAndActiveTrue(Long tournamentId);
    
    @Query("SELECT r FROM SwissRegistration r WHERE r.tournament.id = :tournamentId AND r.user.id = :userId AND r.active = true")
    Optional<SwissRegistration> findActiveRegistration(@Param("tournamentId") Long tournamentId, 
                                                     @Param("userId") Long userId);
    
    @Query("SELECT COUNT(r) FROM SwissRegistration r WHERE r.tournament.id = :tournamentId AND r.active = true")
    int countActiveRegistrations(@Param("tournamentId") Long tournamentId);
}
