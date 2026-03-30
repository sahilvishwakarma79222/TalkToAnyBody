package com.alibou.websocket.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.alibou.websocket.dto.UserDTO;
import com.alibou.websocket.model.User;
import com.alibou.websocket.service.UserService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    // Update user's current room
    @PostMapping("/update-room")
    public ResponseEntity<?> updateUserRoom(@RequestParam Long userId,
                                             @RequestBody Map<String, String> request) {
        try {
            String roomType = request.get("roomType");
            String roomId = request.get("roomId");
            
            userService.updateUserRoom(userId, roomType, roomId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Room updated successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    // Get user by ID
    @GetMapping("/{userId}")
    public ResponseEntity<?> getUser(@PathVariable Long userId) {
        User user = userService.getUserById(userId);
        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
        }
        return ResponseEntity.ok(UserDTO.fromUser(user));
    }
    
    // Get all online users
    @GetMapping("/online")
    public ResponseEntity<List<UserDTO>> getOnlineUsers() {
        List<User> users = userService.getAllOnlineUsers();
        List<UserDTO> userDTOs = users.stream()
                .map(UserDTO::fromUser)
                .collect(Collectors.toList());
        return ResponseEntity.ok(userDTOs);
    }
    
    // Get users in global chat
    @GetMapping("/global")
    public ResponseEntity<List<UserDTO>> getGlobalUsers() {
        List<User> users = userService.getUsersInGlobalChat();
        List<UserDTO> userDTOs = users.stream()
                .map(UserDTO::fromUser)
                .collect(Collectors.toList());
        return ResponseEntity.ok(userDTOs);
    }
}