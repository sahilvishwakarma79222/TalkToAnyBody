package com.alibou.websocket.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibou.websocket.model.Invite;
import com.alibou.websocket.model.Room;
import com.alibou.websocket.model.User;
import com.alibou.websocket.repository.InviteRepository;
import com.alibou.websocket.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class InviteService {
    
    @Autowired
    private InviteRepository inviteRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoomService roomService;
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @Transactional
    public Invite sendInvite(String roomId, Long fromUserId, Long toUserId) {
        // Check if room is not full
        if (roomService.isRoomFull(roomId)) {
            throw new RuntimeException("Room is full");
        }
        
        // Check if user already in room
        if (roomService.getRoomParticipants(roomId).stream().anyMatch(u -> u.getId().equals(toUserId))) {
            throw new RuntimeException("User already in room");
        }
        
        // Create invite (expires in 2 minutes)
        Invite invite = new Invite(roomId, fromUserId, toUserId);
        invite = inviteRepository.save(invite);
        
        // Send real-time notification
        User fromUser = userRepository.findById(fromUserId).orElse(null);
        User toUser = userRepository.findById(toUserId).orElse(null);
        
        if (fromUser != null && toUser != null) {
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "INVITE");
            notification.put("inviteId", invite.getId());
            notification.put("roomId", roomId);
            notification.put("fromUser", fromUser.getUsername());
            notification.put("fromUserId", fromUserId);
            notification.put("expiresAt", invite.getExpiresAt());
            
            messagingTemplate.convertAndSend("/user/" + toUserId + "/queue/invites", notification);
        }
        
        return invite;
    }
    
    @Transactional
    public void acceptInvite(Long inviteId, Long userId) {
        Invite invite = inviteRepository.findById(inviteId)
                .orElseThrow(() -> new RuntimeException("Invite not found"));
        
        if (!invite.getToUserId().equals(userId)) {
            throw new RuntimeException("Not authorized");
        }
        
        if (invite.getExpiresAt().isBefore(LocalDateTime.now())) {
            invite.setStatus("EXPIRED");
            inviteRepository.save(invite);
            throw new RuntimeException("Invite expired");
        }
        
        if (!invite.getStatus().equals("PENDING")) {
            throw new RuntimeException("Invite already " + invite.getStatus());
        }
        
        // Join the room
        Room room = roomService.joinRoom(invite.getRoomId(), 
            roomService.getFixedRoom().getRoomPassword(), userId); // Need to get room password
        
        // Update invite status
        invite.setStatus("ACCEPTED");
        inviteRepository.save(invite);
        
        // Notify the inviter
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "INVITE_ACCEPTED");
        notification.put("roomId", invite.getRoomId());
        notification.put("userId", userId);
        
        messagingTemplate.convertAndSend("/user/" + invite.getFromUserId() + "/queue/invites", notification);
    }
    
    @Transactional
    public void rejectInvite(Long inviteId, Long userId) {
        Invite invite = inviteRepository.findById(inviteId)
                .orElseThrow(() -> new RuntimeException("Invite not found"));
        
        if (!invite.getToUserId().equals(userId)) {
            throw new RuntimeException("Not authorized");
        }
        
        invite.setStatus("REJECTED");
        inviteRepository.save(invite);
        
        // Notify the inviter
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "INVITE_REJECTED");
        notification.put("roomId", invite.getRoomId());
        notification.put("userId", userId);
        
        messagingTemplate.convertAndSend("/user/" + invite.getFromUserId() + "/queue/invites", notification);
    }
    
    public List<Invite> getPendingInvites(Long userId) {
        return inviteRepository.findByToUserIdAndStatus(userId, "PENDING");
    }
    
    @Transactional
    public void expireOldInvites() {
        inviteRepository.expireOldInvites(LocalDateTime.now());
    }
}