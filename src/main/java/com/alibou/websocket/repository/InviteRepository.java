package com.alibou.websocket.repository;
 import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.alibou.websocket.model.Invite;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InviteRepository extends JpaRepository<Invite, Long> {
    
    @Query("SELECT i FROM Invite i WHERE i.toUserId = :toUserId AND i.status = :status")
    List<Invite> findByToUserIdAndStatus(@Param("toUserId") Long toUserId, 
                                          @Param("status") String status);
    
    @Query("SELECT i FROM Invite i WHERE i.roomId = :roomId AND i.toUserId = :toUserId AND i.status = :status")
    Optional<Invite> findByRoomIdAndToUserIdAndStatus(@Param("roomId") String roomId,
                                                       @Param("toUserId") Long toUserId,
                                                       @Param("status") String status);
    
    @Modifying
    @Transactional
    @Query("UPDATE Invite i SET i.status = 'EXPIRED' WHERE i.status = 'PENDING' AND i.expiresAt < :now")
    void expireOldInvites(@Param("now") LocalDateTime now);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM Invite i WHERE i.expiresAt < :cutoff")
    void deleteExpiredInvites(@Param("cutoff") LocalDateTime cutoff);
}