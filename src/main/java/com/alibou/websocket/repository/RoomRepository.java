package com.alibou.websocket.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.alibou.websocket.model.Room;

import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    
    Optional<Room> findByRoomId(String roomId);
    
    boolean existsByRoomId(String roomId);
    
    @Query("SELECT r FROM Room r WHERE r.isFixed = false")
    List<Room> findNonFixedRooms();
    
    @Modifying
    @Transactional
    @Query("DELETE FROM Room r WHERE r.isFixed = false AND r.roomId NOT IN " +
           "(SELECT p.id.roomId FROM RoomParticipant p)")
    void deleteEmptyRooms();
    
    // Alternative simpler method - if above doesn't work
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM rooms WHERE is_fixed = false AND room_id NOT IN " +
           "(SELECT DISTINCT room_id FROM room_participants)", 
           nativeQuery = true)
    void deleteEmptyRoomsNative();
}