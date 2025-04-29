package org.example.popitkan5.tournament.repository;

import org.example.popitkan5.tournament.model.TournamentParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TournamentParticipantRepository extends JpaRepository<TournamentParticipant, Long> {
    List<TournamentParticipant> findByTournamentId(Long tournamentId);
    
    Optional<TournamentParticipant> findByTournamentIdAndUserId(Long tournamentId, Long userId);
    
    boolean existsByTournamentIdAndUserId(Long tournamentId, Long userId);
    
    @Query("SELECT COUNT(tp) FROM TournamentParticipant tp WHERE tp.tournamentId = ?1")
    int countByTournamentId(Long tournamentId);
    
    void deleteByTournamentIdAndUserId(Long tournamentId, Long userId);
}
