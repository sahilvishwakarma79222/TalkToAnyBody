package com.alibou.websocket.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.alibou.websocket.dto.MessageDTO;
import com.alibou.websocket.dto.UserDTO;
import com.alibou.websocket.model.Message;
import com.alibou.websocket.model.User;
import com.alibou.websocket.service.ChatService;
import com.alibou.websocket.service.RoomService;
import com.alibou.websocket.service.UserService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class ChatController {
    
    @Autowired
    private ChatService chatService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    
    @Autowired
    private RoomService roomService;
    // Send message to global chat
 // Send message to global chat
    @MessageMapping("/chat.send")
    @SendTo("/topic/global")
    public MessageDTO sendMessage(@Payload MessageDTO messageDTO, 
                                   SimpMessageHeaderAccessor headerAccessor) {
        System.out.println("📨 Received: " + messageDTO.getMessageType() + " from " + messageDTO.getUsername());
        
        // Get user info
        String sessionId = (String) headerAccessor.getSessionAttributes().get("sessionId");
        User user = null;
        
        if (sessionId != null) {
            user = userService.getUserBySessionId(sessionId);
        }
        
        if (user == null && messageDTO.getUserId() != null) {
            user = userService.getUserById(messageDTO.getUserId());
        }
        
        if (user == null) {
            System.out.println("❌ User not found!");
            return null;
        }
        
        // For IMAGE type, don't save to DB (temporary only)
        if ("IMAGE".equals(messageDTO.getMessageType())) {
            MessageDTO dto = new MessageDTO();
            dto.setContent(messageDTO.getContent());
            dto.setMessageType("IMAGE");
            dto.setUserId(user.getId());
            dto.setUsername(user.getUsername());
            dto.setRoomType("GLOBAL");
            dto.setRoomId(null);
            dto.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
            dto.setImageData(messageDTO.getImageData());
            return dto;
        }
        
        // Save text/emoji to database
        Message message = chatService.saveMessage(
            messageDTO.getContent(),
            messageDTO.getMessageType(),
            user.getId(),
            user.getUsername(),
            "GLOBAL",
            null
        );
        
        return MessageDTO.fromMessage(message);
    }
    
    // Get online users
    @MessageMapping("/chat.users")
    @SendTo("/topic/users")
    public List<UserDTO> getOnlineUsers() {
        System.out.println("📋 Getting online users");
        List<User> users = userService.getAllOnlineUsers();
        List<UserDTO> userDTOs = users.stream()
                .map(UserDTO::fromUser)
                .collect(Collectors.toList());
        System.out.println("   Online users: " + userDTOs.size());
        return userDTOs;
    }
    
    // User join notification
    @MessageMapping("/chat.join")
    @SendTo("/topic/global")
    public MessageDTO userJoined(@Payload MessageDTO messageDTO,
                                  SimpMessageHeaderAccessor headerAccessor) {
        System.out.println("👋 User joining: " + messageDTO.getUsername());
        
        String sessionId = (String) headerAccessor.getSessionAttributes().get("sessionId");
        User user = userService.getUserBySessionId(sessionId);
        
        if (user != null) {
            // Store user info in WebSocket session
            headerAccessor.getSessionAttributes().put("userId", user.getId());
            headerAccessor.getSessionAttributes().put("username", user.getUsername());
            headerAccessor.getSessionAttributes().put("sessionId", sessionId);
            
            // Broadcast join message
            Message joinMessage = chatService.saveMessage(
                user.getUsername() + " joined the chat!",
                "TEXT",
                null,
                "System",
                "GLOBAL",
                null
            );
            
            // Notify all users to update user list
            broadcastOnlineUsers();
            
            System.out.println("✅ User joined: " + user.getUsername());
            
            return MessageDTO.fromMessage(joinMessage);
        }
        return null;
    }
    
    // User leave notification
    @MessageMapping("/chat.leave")
    public void userLeave(SimpMessageHeaderAccessor headerAccessor) {
        Long userId = (Long) headerAccessor.getSessionAttributes().get("userId");
        String username = (String) headerAccessor.getSessionAttributes().get("username");
        
        if (userId != null && username != null) {
            System.out.println("👋 User leaving: " + username);
            
            // Broadcast leave message
            Message leaveMessage = chatService.saveMessage(
                username + " left the chat!",
                "TEXT",
                null,
                "System",
                "GLOBAL",
                null
            );
            
            messagingTemplate.convertAndSend("/topic/global", 
                MessageDTO.fromMessage(leaveMessage));
            
            // Remove user from active users
            userService.removeUser(userId);
            
            // Update online users list
            broadcastOnlineUsers();
        }
    }
    
    private void broadcastOnlineUsers() {
        List<UserDTO> onlineUsers = userService.getAllOnlineUsers().stream()
                .map(UserDTO::fromUser)
                .collect(Collectors.toList());
        messagingTemplate.convertAndSend("/topic/users", onlineUsers);
        System.out.println("📢 Broadcasting online users: " + onlineUsers.size());
    }
    
 // User joins private room
    @MessageMapping("/chat.private.join")
    public void joinPrivateRoom(@Payload Map<String, Object> payload,
                                 SimpMessageHeaderAccessor headerAccessor) {
        String roomId = (String) payload.get("roomId");
        Long userId = ((Number) payload.get("userId")).longValue();
        String username = (String) payload.get("username");
        
        System.out.println("🔒 User " + username + " joined private room: " + roomId);
        
        // Store in session
        headerAccessor.getSessionAttributes().put("currentRoomId", roomId);
        headerAccessor.getSessionAttributes().put("currentRoomType", "PRIVATE");
        
        // Notify room participants
        Message joinMessage = chatService.saveMessage(
            username + " joined the private room!",
            "TEXT",
            null,
            "System",
            "PRIVATE",
            roomId
        );
        
        messagingTemplate.convertAndSend("/topic/room/" + roomId, 
            MessageDTO.fromMessage(joinMessage));
    }

    // Send message to private room
 // Send message to private room
    @MessageMapping("/chat.private.send")
    public void sendPrivateMessage(@Payload MessageDTO messageDTO) {
        System.out.println("📨 Private message to " + messageDTO.getRoomId() + 
                           " Type: " + messageDTO.getMessageType());
        
        // For IMAGE type, don't save to DB
        if ("IMAGE".equals(messageDTO.getMessageType())) {
            MessageDTO dto = new MessageDTO();
            dto.setContent(messageDTO.getContent());
            dto.setMessageType("IMAGE");
            dto.setUserId(messageDTO.getUserId());
            dto.setUsername(messageDTO.getUsername());
            dto.setRoomType("PRIVATE");
            dto.setRoomId(messageDTO.getRoomId());
            dto.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
            dto.setImageData(messageDTO.getImageData());
            
            messagingTemplate.convertAndSend("/topic/room/" + messageDTO.getRoomId(), dto);
            return;
        }
        
        // Save text/emoji to database
        Message message = chatService.saveMessage(
            messageDTO.getContent(),
            messageDTO.getMessageType(),
            messageDTO.getUserId(),
            messageDTO.getUsername(),
            "PRIVATE",
            messageDTO.getRoomId()
        );
        
        messagingTemplate.convertAndSend("/topic/room/" + messageDTO.getRoomId(), 
            MessageDTO.fromMessage(message));
    }
    
 // Call Signaling - Offer
    @MessageMapping("/call.offer")
    public void handleCallOffer(@Payload Map<String, Object> payload) {
        String roomId = (String) payload.get("roomId");
        Long fromUserId = ((Number) payload.get("fromUserId")).longValue();
        String fromUsername = (String) payload.get("fromUsername");
        String callType = (String) payload.get("callType");
        Map<String, Object> offer = (Map<String, Object>) payload.get("offer");
        
        System.out.println("📞 Call offer from " + fromUsername + " in room " + roomId);
        
        // Get other participant in the room
        List<User> participants = roomService.getRoomParticipants(roomId);
        User otherUser = participants.stream()
                .filter(u -> !u.getId().equals(fromUserId))
                .findFirst()
                .orElse(null);
        
        if (otherUser != null) {
            messagingTemplate.convertAndSendToUser(
                otherUser.getId().toString(),
                "/queue/call.offer",
                Map.of(
                    "roomId", roomId,
                    "fromUserId", fromUserId,
                    "fromUsername", fromUsername,
                    "callType", callType,
                    "offer", offer
                )
            );
        }
    }

    // Call Signaling - Answer
    @MessageMapping("/call.answer")
    public void handleCallAnswer(@Payload Map<String, Object> payload) {
        String roomId = (String) payload.get("roomId");
        Long toUserId = ((Number) payload.get("toUserId")).longValue();
        Map<String, Object> answer = (Map<String, Object>) payload.get("answer");
        
        messagingTemplate.convertAndSendToUser(
            toUserId.toString(),
            "/queue/call.answer",
            Map.of("roomId", roomId, "answer", answer)
        );
    }

    // Call Signaling - ICE Candidate
    @MessageMapping("/call.ice")
    public void handleIceCandidate(@Payload Map<String, Object> payload) {
        String roomId = (String) payload.get("roomId");
        Long fromUserId = ((Number) payload.get("fromUserId")).longValue();
        Map<String, Object> candidate = (Map<String, Object>) payload.get("candidate");
        
        // Get other participant
        List<User> participants = roomService.getRoomParticipants(roomId);
        User otherUser = participants.stream()
                .filter(u -> !u.getId().equals(fromUserId))
                .findFirst()
                .orElse(null);
        
        if (otherUser != null) {
            messagingTemplate.convertAndSendToUser(
                otherUser.getId().toString(),
                "/queue/call.ice",
                Map.of("candidate", candidate)
            );
        }
    }

    // Call Signaling - Reject
    @MessageMapping("/call.reject")
    public void handleCallReject(@Payload Map<String, Object> payload) {
        String roomId = (String) payload.get("roomId");
        Long toUserId = ((Number) payload.get("toUserId")).longValue();
        String reason = (String) payload.getOrDefault("reason", "rejected");
        
        messagingTemplate.convertAndSendToUser(
            toUserId.toString(),
            "/queue/call.reject",
            Map.of("roomId", roomId, "reason", reason)
        );
    }

    // Call Signaling - End
    @MessageMapping("/call.end")
    public void handleCallEnd(@Payload Map<String, Object> payload) {
        String roomId = (String) payload.get("roomId");
        Long fromUserId = ((Number) payload.get("fromUserId")).longValue();
        
        // Get other participant
        List<User> participants = roomService.getRoomParticipants(roomId);
        User otherUser = participants.stream()
                .filter(u -> !u.getId().equals(fromUserId))
                .findFirst()
                .orElse(null);
        
        if (otherUser != null) {
            messagingTemplate.convertAndSendToUser(
                otherUser.getId().toString(),
                "/queue/call.end",
                Map.of("roomId", roomId)
            );
        }
    }
}