package com.alibou.websocket.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.alibou.websocket.model.Message;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    
    // Find messages by room (all messages)
    @Query("SELECT m FROM Message m WHERE m.roomType = :roomType AND " +
           "(:roomId IS NULL OR m.roomId = :roomId) " +
           "ORDER BY m.timestamp DESC")
    List<Message> findMessagesByRoom(@Param("roomType") String roomType, 
                                      @Param("roomId") String roomId);
    
    // Find recent messages with limit
    @Query(value = "SELECT * FROM messages WHERE room_type = ?1 AND " +
           "(?2 IS NULL OR room_id = ?2) " +
           "ORDER BY timestamp DESC LIMIT ?3", 
           nativeQuery = true)
    List<Message> findRecentMessages(String roomType, String roomId, int limit);
    
    // Count messages in a room
    long countByRoomTypeAndRoomId(String roomType, String roomId);
    
    // Delete oldest messages (native query)
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM messages WHERE id IN (" +
           "SELECT id FROM messages WHERE room_type = ?1 AND " +
           "(?2 IS NULL OR room_id = ?2) " +
           "ORDER BY timestamp ASC LIMIT ?3)", 
           nativeQuery = true)
    void deleteOldestMessages(String roomType, String roomId, long count);
    
    // Delete messages by IDs
    @Modifying
    @Transactional
    @Query("DELETE FROM Message m WHERE m.id IN :ids")
    void deleteMessagesByIds(@Param("ids") List<Long> ids);
    
    // Delete messages older than cutoff
    @Modifying
    @Transactional
    @Query("DELETE FROM Message m WHERE m.timestamp < :cutoff")
    void deleteMessagesOlderThan(@Param("cutoff") LocalDateTime cutoff);
}