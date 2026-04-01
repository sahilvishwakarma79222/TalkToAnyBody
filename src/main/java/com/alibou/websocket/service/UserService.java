package com.alibou.websocket.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibou.websocket.model.User;
import com.alibou.websocket.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoomService roomService;
    
    @Transactional
    public User createUser(String username, String sessionId) {
        // ✅ Remove any existing user with this session first
        userRepository.deleteBySessionId(sessionId);
        
        // Create new user
        User user = new User();
        user.setUsername(username);
        user.setSessionId(sessionId);
        user.setCurrentRoomType("GLOBAL");
        user.setCurrentRoomId(null);
        user.setLastActive(LocalDateTime.now());
        
        return userRepository.save(user);
    }
    
    @Transactional
    public void removeUser(Long userId) {
        // ✅ First remove user from all rooms
        try {
            roomService.forceLeaveAllRooms(userId);
        } catch (Exception e) {
            System.out.println("Error removing from rooms: " + e.getMessage());
        }
        
        // ✅ Then delete the user
        userRepository.deleteById(userId);
        System.out.println("🗑️ User deleted: " + userId);
    }
    
    // ✅ Clean up old/inactive users
    @Transactional
    public void cleanupInactiveUsers() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(5);
        List<User> inactiveUsers = userRepository.findInactiveUsers(cutoff);
        
        for (User user : inactiveUsers) {
            System.out.println("🧹 Cleaning inactive user: " + user.getUsername() + " (last active: " + user.getLastActive() + ")");
            removeUser(user.getId());
        }
    }
    
    public List<User> getAllOnlineUsers() {
        // Users active in last 5 minutes
        LocalDateTime activeThreshold = LocalDateTime.now().minusMinutes(5);
        return userRepository.findActiveUsers(activeThreshold);
    }
    
    @Transactional
    public void updateUserActivity(Long userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setLastActive(LocalDateTime.now());
            userRepository.save(user);
        });
    }
    
    public User getUserById(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }
    
    public User getUserBySessionId(String sessionId) {
        return userRepository.findBySessionId(sessionId).orElse(null);
    }
    
    @Transactional
    public void updateUserRoom(Long userId, String roomType, String roomId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setCurrentRoomType(roomType);
            user.setCurrentRoomId(roomId);
            user.setLastActive(LocalDateTime.now());
            userRepository.save(user);
        });
    }
    
    public List<User> getUsersInGlobalChat() {
        return userRepository.findByCurrentRoomType("GLOBAL");
    }
    
    // ✅ Get all users (for debugging)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    // ✅ Force delete all users (for cleanup)
    @Transactional
    public void deleteAllUsers() {
        userRepository.deleteAll();
        System.out.println("🗑️ All users deleted");
    }
}