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

import java.util.List;
import java.util.UUID;

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
        Room room = roomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));
        
        if (!room.getRoomPassword().equals(password)) {
            throw new RuntimeException("Invalid password");
        }
        
        long participantCount = participantRepository.countByRoomId(roomId);
        if (participantCount >= MAX_PARTICIPANTS) {
            throw new RuntimeException("Room is occupied (max 2 users)");
        }
        
        // Check if user already in room
        boolean alreadyInRoom = participantRepository.findByRoomIdAndUserId(roomId, userId).isPresent();
        if (alreadyInRoom) {
            throw new RuntimeException("Already in this room");
        }
        
        // Add participant
        RoomParticipant participant = new RoomParticipant(roomId, userId);
        participantRepository.save(participant);
        
        // Update user's current room
        User user = userRepository.findById(userId).orElseThrow();
        user.setCurrentRoomType("PRIVATE");
        user.setCurrentRoomId(roomId);
        userRepository.save(user);
        
        return room;
    }
    
    @Transactional
    public void leaveRoom(String roomId, Long userId) {
        participantRepository.deleteByRoomIdAndUserId(roomId, userId);
        
        // Update user's current room back to global
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            user.setCurrentRoomType("GLOBAL");
            user.setCurrentRoomId(null);
            userRepository.save(user);
        }
        
        // If room is empty, delete it (if not fixed)
        long participantCount = participantRepository.countByRoomId(roomId);
        if (participantCount == 0) {
            Room room = roomRepository.findByRoomId(roomId).orElse(null);
            if (room != null && !room.getIsFixed()) {
                roomRepository.delete(room);
            }
        }
    }
    
    public Room getFixedRoom() {
        return roomRepository.findByRoomId(fixedRoomId)
                .orElseGet(() -> {
                    Room fixed = new Room(fixedRoomId, fixedRoomPassword, "system", true);
                    return roomRepository.save(fixed);
                });
    }
    
    public List<User> getRoomParticipants(String roomId) {
        List<RoomParticipant> participants = participantRepository.findByRoomId(roomId);
        return participants.stream()
                .map(p -> userRepository.findById(p.getUserId()).orElse(null))
                .filter(u -> u != null)
                .toList();
    }
    
    public boolean isRoomFull(String roomId) {
        return participantRepository.countByRoomId(roomId) >= MAX_PARTICIPANTS;
    }
}