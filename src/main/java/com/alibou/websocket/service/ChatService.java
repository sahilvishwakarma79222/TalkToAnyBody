package com.alibou.websocket.service;
  import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibou.websocket.model.Message;
import com.alibou.websocket.repository.MessageRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatService {
    
    @Autowired
    private MessageRepository messageRepository;
    
    private static final int MAX_MESSAGES_PER_ROOM = 1000;
    
    @Transactional
    public Message saveMessage(String content, String messageType, Long userId, 
                               String username, String roomType, String roomId) {
        // Create and save message
        Message message = new Message(content, messageType, userId, username, roomType, roomId);
        message = messageRepository.save(message);
        
        // Check message count and delete oldest if exceeding limit
        long count = messageRepository.countByRoomTypeAndRoomId(roomType, roomId);
        if (count > MAX_MESSAGES_PER_ROOM) {
            long excess = count - MAX_MESSAGES_PER_ROOM;
            // Use the native query to delete oldest messages
            messageRepository.deleteOldestMessages(roomType, roomId, excess);
        }
        
        return message;
    }
    
    public List<Message> getRecentMessages(String roomType, String roomId, int limit) {
        // Use the native query method
        return messageRepository.findRecentMessages(roomType, roomId, limit);
    }
    
    public List<Message> getAllMessages(String roomType, String roomId) {
        return messageRepository.findMessagesByRoom(roomType, roomId);
    }
    
    @Transactional
    public void deleteOldMessages(LocalDateTime cutoff) {
        messageRepository.deleteMessagesOlderThan(cutoff);
    }
    
    @Transactional
    public void deleteMessagesByIds(List<Long> ids) {
        if (ids != null && !ids.isEmpty()) {
            messageRepository.deleteMessagesByIds(ids);
        }
    }
}