package com.alibou.websocket.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "room_participants")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomParticipant {
    
    @EmbeddedId
    private RoomParticipantId id;
    
    @Column(name = "joined_at")
    private LocalDateTime joinedAt = LocalDateTime.now();
    
    // Convenience methods
    public String getRoomId() {
        return id != null ? id.getRoomId() : null;
    }
    
    public Long getUserId() {
        return id != null ? id.getUserId() : null;
    }
    
    public void setRoomId(String roomId) {
        if (id == null) {
            id = new RoomParticipantId();
        }
        id.setRoomId(roomId);
    }
    
    public void setUserId(Long userId) {
        if (id == null) {
            id = new RoomParticipantId();
        }
        id.setUserId(userId);
    }
    
    // Constructor for easy creation
    public RoomParticipant(String roomId, Long userId) {
        this.id = new RoomParticipantId(roomId, userId);
        this.joinedAt = LocalDateTime.now();
    }
}