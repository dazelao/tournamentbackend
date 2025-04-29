package org.example.popitkan5.repository;

import org.example.popitkan5.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByTelegram(String telegram);
    Optional<User> findByEaId(String eaId);
    boolean existsByUsername(String username);
    boolean existsByTelegram(String telegram);
    boolean existsByEaId(String eaId);
    List<User> findByUsernameContainingIgnoreCase(String query);
    
    // Поиск пользователей по ключу и значению атрибута
    @Query("SELECT u FROM User u JOIN u.attributes a WHERE KEY(a) = :key AND VALUE(a) = :value")
    List<User> findByAttributeKeyAndValue(@Param("key") String key, @Param("value") String value);
    
    // Поиск пользователей по ключу атрибута
    @Query("SELECT u FROM User u JOIN u.attributes a WHERE KEY(a) = :key")
    List<User> findByAttributeKey(@Param("key") String key);
    
    // Поиск пользователей по значению атрибута
    @Query("SELECT u FROM User u JOIN u.attributes a WHERE VALUE(a) = :value")
    List<User> findByAttributeValue(@Param("value") String value);
    
    // Поиск пользователей по нескольким ключам и значениям атрибутов
    @Query("SELECT DISTINCT u FROM User u JOIN u.attributes a WHERE (KEY(a) IN :keys)")
    List<User> findByAttributeKeysIn(@Param("keys") List<String> keys);
}