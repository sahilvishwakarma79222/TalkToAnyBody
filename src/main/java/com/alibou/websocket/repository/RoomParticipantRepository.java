package com.alibou.websocket.repository;
  import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.alibou.websocket.model.RoomParticipant;
import com.alibou.websocket.model.RoomParticipantId;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomParticipantRepository extends JpaRepository<RoomParticipant, RoomParticipantId> {
    
    @Query("SELECT COUNT(p) FROM RoomParticipant p WHERE p.id.roomId = :roomId")
    long countByRoomId(@Param("roomId") String roomId);
    
    @Query("SELECT p FROM RoomParticipant p WHERE p.id.roomId = :roomId")
    List<RoomParticipant> findByRoomId(@Param("roomId") String roomId);
    
    @Query("SELECT p FROM RoomParticipant p WHERE p.id.roomId = :roomId AND p.id.userId = :userId")
    Optional<RoomParticipant> findByRoomIdAndUserId(@Param("roomId") String roomId, 
                                                     @Param("userId") Long userId);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM RoomParticipant p WHERE p.id.roomId = :roomId AND p.id.userId = :userId")
    void deleteByRoomIdAndUserId(@Param("roomId") String roomId, @Param("userId") Long userId);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM RoomParticipant p WHERE p.id.roomId = :roomId")
    void deleteAllByRoomId(@Param("roomId") String roomId);
}