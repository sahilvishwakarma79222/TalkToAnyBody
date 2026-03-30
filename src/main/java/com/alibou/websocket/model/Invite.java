package com.alibou.websocket.model;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "invites")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Invite {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "room_id", nullable = false, length = 100)
    private String roomId;
    
    @Column(name = "from_user_id", nullable = false)
    private Long fromUserId;
    
    @Column(name = "to_user_id", nullable = false)
    private Long toUserId;
    
    @Column(length = 20)
    private String status = "PENDING"; // PENDING, ACCEPTED, REJECTED, EXPIRED
    
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
    
    // Constructor for creating new invite (expires in 2 minutes)
    public Invite(String roomId, Long fromUserId, Long toUserId) {
        this.roomId = roomId;
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
        this.createdAt = LocalDateTime.now();
        this.expiresAt = LocalDateTime.now().plusMinutes(2);
        this.status = "PENDING";
    }
}