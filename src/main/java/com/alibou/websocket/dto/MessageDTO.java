package com.alibou.websocket.dto;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.alibou.websocket.model.Message;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageDTO {
    private Long id;
    private String content;
    private String messageType; // TEXT, EMOJI, IMAGE
    private Long userId;
    private String username;
    private String roomType; // GLOBAL, PRIVATE
    private String roomId;
    private String timestamp;
    private String imageData; // For base64 image (temporary, not stored in DB)
    
    public MessageDTO(String content, String messageType, Long userId, 
                      String username, String roomType, String roomId) {
        this.content = content;
        this.messageType = messageType;
        this.userId = userId;
        this.username = username;
        this.roomType = roomType;
        this.roomId = roomId;
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
    }
    
    public static MessageDTO fromMessage(Message message) {
        MessageDTO dto = new MessageDTO();
        dto.setId(message.getId());
        dto.setContent(message.getContent());
        dto.setMessageType(message.getMessageType());
        dto.setUserId(message.getUserId());
        dto.setUsername(message.getUsername());
        dto.setRoomType(message.getRoomType());
        dto.setRoomId(message.getRoomId());
        dto.setTimestamp(message.getTimestamp().format(DateTimeFormatter.ofPattern("HH:mm")));
        return dto;
    }
}