package com.alibou.websocket.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import com.alibou.websocket.service.RoomService;
import com.alibou.websocket.service.UserService;

@Component
public class WebSocketEventListener {
    
    @Autowired
    private SimpMessageSendingOperations messagingTemplate;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private RoomService roomService;
    
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        System.out.println("🔌 New WebSocket connection established");
    }
    
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        Long userId = (Long) headerAccessor.getSessionAttributes().get("userId");
        String username = (String) headerAccessor.getSessionAttributes().get("username");
        
        if (userId != null) {
            System.out.println("👋 User disconnected: " + (username != null ? username : userId));
            
            // ✅ Force remove user from ALL rooms
            try {
                roomService.forceLeaveAllRooms(userId);
            } catch (Exception e) {
                System.out.println("Error leaving rooms: " + e.getMessage());
            }
            
            // ✅ Delete the user completely
            try {
                userService.removeUser(userId);
                System.out.println("   User deleted from database");
            } catch (Exception e) {
                System.out.println("Error deleting user: " + e.getMessage());
            }
            
            // Broadcast leave message
            if (username != null) {
                messagingTemplate.convertAndSend("/topic/global", username + " left the chat!");
            }
            
            // Update online users list
            messagingTemplate.convertAndSend("/topic/users", userService.getAllOnlineUsers());
        }
    }
}