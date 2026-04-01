package com.alibou.websocket.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibou.websocket.model.Room;
import com.alibou.websocket.model.RoomParticipant;
import com.alibou.websocket.model.User;
import com.alibou.websocket.repository.RoomParticipantRepository;
import com.alibou.websocket.repository.RoomRepository;
import com.alibou.websocket.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RoomService {
    
    @Autowired
    private RoomRepository roomRepository;
    
    @Autowired
    private RoomParticipantRepository participantRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Value("${fixed.room.id:jojo}")
    private String fixedRoomId;
    
    @Value("${fixed.room.password:123456}")
    private String fixedRoomPassword;
    
    private static final int MAX_PARTICIPANTS = 2;
    
    @Transactional
    public Room createPrivateRoom(String password, String creator) {
        String roomId = UUID.randomUUID().toString().substring(0, 8);
        Room room = new Room(roomId, password, creator, false);
        return roomRepository.save(room);
    }
    
    @Transactional
    public Room joinRoom(String roomId, String password, Long userId) {
        System.out.println("🔍 Joining room - roomId: " + roomId + ", userId: " + userId);
        
        // ✅ CRITICAL: First delete ANY existing participant entry for this user in ANY room
        // This prevents the "already in room" error
        participantRepository.deleteByUserId(userId);
        System.out.println("   Deleted any existing participant entries for user: " + userId);
        
        Room room = roomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));
        
        // Password validation
        if (room.getIsFixed()) {
            if (!fixedRoomPassword.equals(password)) {
                throw new RuntimeException("Invalid password");
            }
        } else {
            if (!room.getRoomPassword().equals(password)) {
                throw new RuntimeException("Invalid password");
            }
        }
        
        // Clean stale participants before checking count
        cleanupStaleParticipants(roomId);
        
        // Check room capacity
        long participantCount = participantRepository.countByRoomId(roomId);
        if (participantCount >= MAX_PARTICIPANTS) {
            throw new RuntimeException("Room is full (max 2 users)");
        }
        
        // ✅ Add participant - use save, NOT update
        RoomParticipant participant = new RoomParticipant(roomId, userId);
        participantRepository.save(participant);
        System.out.println("   Added participant to room: " + roomId);
        
        // Update user's current room
        User user = userRepository.findById(userId).orElseThrow();
        user.setCurrentRoomType("PRIVATE");
        user.setCurrentRoomId(roomId);
        user.setLastActive(LocalDateTime.now());
        userRepository.save(user);
        
        System.out.println("✅ User " + user.getUsername() + " joined room: " + roomId);
        
        return room;
    }
    
    // ✅ Force remove user from ALL rooms (delete all entries)
    @Transactional
    public void forceLeaveAllRooms(Long userId) {
        System.out.println("🗑️ Force removing user " + userId + " from all rooms");
        
        // Delete all participant entries for this user
        participantRepository.deleteByUserId(userId);
        
        // Update user's current room
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            user.setCurrentRoomType("GLOBAL");
            user.setCurrentRoomId(null);
            user.setLastActive(LocalDateTime.now());
            userRepository.save(user);
        }
        
        // Clean up empty rooms
        List<Room> allRooms = roomRepository.findAll();
        for (Room room : allRooms) {
            cleanupEmptyRoom(room.getRoomId());
        }
    }
    
    @Transactional
    public void leaveRoom(String roomId, Long userId) {
        System.out.println("🚪 User " + userId + " leaving room: " + roomId);
        
        // Delete participant entry
        participantRepository.deleteByRoomIdAndUserId(roomId, userId);
        
        // Update user's current room
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            user.setCurrentRoomType("GLOBAL");
            user.setCurrentRoomId(null);
            user.setLastActive(LocalDateTime.now());
            userRepository.save(user);
        }
        
        cleanupEmptyRoom(roomId);
    }
    
    // ✅ Cleanup stale participants
    private void cleanupStaleParticipants(String roomId) {
        List<RoomParticipant> participants = participantRepository.findByRoomId(roomId);
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(2);
        
        for (RoomParticipant participant : participants) {
            User user = userRepository.findById(participant.getUserId()).orElse(null);
            
            if (user == null || 
                user.getLastActive() == null || 
                user.getLastActive().isBefore(cutoff)) {
                participantRepository.deleteByRoomIdAndUserId(roomId, participant.getUserId());
                System.out.println("🧹 Cleaned stale participant: userId=" + participant.getUserId());
            }
        }
    }
    
    private void cleanupEmptyRoom(String roomId) {
        long participantCount = participantRepository.countByRoomId(roomId);
        if (participantCount == 0) {
            Room room = roomRepository.findByRoomId(roomId).orElse(null);
            if (room != null && !room.getIsFixed()) {
                roomRepository.delete(room);
                System.out.println("🗑️ Deleted empty room: " + roomId);
            }
        }
    }
    
    public Room getFixedRoom() {
        cleanupStaleParticipants(fixedRoomId);
        
        Room fixed = roomRepository.findByRoomId(fixedRoomId)
                .orElseGet(() -> {
                    Room newFixed = new Room(fixedRoomId, fixedRoomPassword, "system", true);
                    return roomRepository.save(newFixed);
                });
        
        return fixed;
    }
    
    public List<User> getRoomParticipants(String roomId) {
        cleanupStaleParticipants(roomId);
        List<RoomParticipant> participants = participantRepository.findByRoomId(roomId);
        return participants.stream()
                .map(p -> userRepository.findById(p.getUserId()).orElse(null))
                .filter(u -> u != null)
                .collect(Collectors.toList());
    }
    
    public boolean isRoomFull(String roomId) {
        cleanupStaleParticipants(roomId);
        return participantRepository.countByRoomId(roomId) >= MAX_PARTICIPANTS;
    }
    
    @Transactional
    public void cleanupAllStaleParticipants() {
        System.out.println("🧹 Running full stale participant cleanup...");
        
        // Delete all participants for users that don't exist
        participantRepository.deleteOrphanParticipants();
        
        // Delete participants for inactive users
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(2);
        List<User> inactiveUsers = userRepository.findInactiveUsers(cutoff);
        
        for (User user : inactiveUsers) {
            forceLeaveAllRooms(user.getId());
        }
        
        // Clean empty rooms
        roomRepository.deleteEmptyRoomsNative();
        
        System.out.println("✅ Stale participant cleanup completed");
    }
}