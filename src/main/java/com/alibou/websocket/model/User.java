package com.alibou.websocket.model;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 50)
    private String username;
    
    @Column(unique = true, nullable = false, length = 100)
    private String sessionId;
    
    @Column(name = "current_room_type")
    private String currentRoomType = "GLOBAL"; // GLOBAL or PRIVATE
    
    @Column(name = "current_room_id")
    private String currentRoomId;
    
    @Column(name = "last_active")
    private LocalDateTime lastActive = LocalDateTime.now();
    
    // Helper method to update last active time
    public void updateLastActive() {
        this.lastActive = LocalDateTime.now();
    }
}