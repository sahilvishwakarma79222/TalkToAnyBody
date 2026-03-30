package com.alibou.websocket.model;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "rooms")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Room {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "room_id", unique = true, nullable = false, length = 100)
    private String roomId;
    
    @Column(name = "room_password", nullable = false, length = 100)
    private String roomPassword;
    
    @Column(name = "created_by", length = 50)
    private String createdBy;
    
    @Column(name = "is_fixed")
    private Boolean isFixed = false;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    // Constructor for creating new room
    public Room(String roomId, String roomPassword, String createdBy, Boolean isFixed) {
        this.roomId = roomId;
        this.roomPassword = roomPassword;
        this.createdBy = createdBy;
        this.isFixed = isFixed;
        this.createdAt = LocalDateTime.now();
    }
}