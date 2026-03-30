package com.alibou.websocket.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.alibou.websocket.model.User;

import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findBySessionId(String sessionId);
    
    List<User> findAllByOrderByUsernameAsc();
    
    @Query("SELECT u FROM User u WHERE u.lastActive > :time")
    List<User> findActiveUsers(@Param("time") LocalDateTime time);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM User u WHERE u.lastActive < :time")
    void deleteInactiveUsers(@Param("time") LocalDateTime time);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM User u WHERE u.sessionId = :sessionId")
    void deleteBySessionId(@Param("sessionId") String sessionId);
    
    long countByCurrentRoomId(String roomId);
    
    @Query("SELECT u FROM User u WHERE u.currentRoomType = 'GLOBAL'")
    List<User> findByCurrentRoomType(String roomType);
}