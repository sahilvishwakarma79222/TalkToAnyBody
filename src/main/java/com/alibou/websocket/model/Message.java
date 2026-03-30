package com.alibou.websocket.model;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(columnDefinition = "TEXT")
    private String content;
    
    @Column(name = "message_type", nullable = false, length = 20)
    private String messageType; // TEXT, EMOJI, IMAGE
    
    @Column(name = "user_id")
    private Long userId;
    
    @Column(nullable = false, length = 50)
    private String username;
    
    @Column(name = "room_type", nullable = false, length = 20)
    private String roomType; // GLOBAL, PRIVATE
    
    @Column(name = "room_id", length = 100)
    private String roomId; // null for global
    
    private LocalDateTime timestamp = LocalDateTime.now();
    
    // Constructor for creating new message
    public Message(String content, String messageType, Long userId, String username, 
                   String roomType, String roomId) {
        this.content = content;
        this.messageType = messageType;
        this.userId = userId;
        this.username = username;
        this.roomType = roomType;
        this.roomId = roomId;
        this.timestamp = LocalDateTime.now();
    }
}