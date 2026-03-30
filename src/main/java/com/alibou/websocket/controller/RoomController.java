package com.alibou.websocket.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.alibou.websocket.model.Room;
import com.alibou.websocket.model.User;
import com.alibou.websocket.service.RoomService;
import com.alibou.websocket.service.UserService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/room")
public class RoomController {
    
    @Autowired
    private RoomService roomService;
    
    @Autowired
    private UserService userService;
    
    // Create private room
    @PostMapping("/create")
    public ResponseEntity<?> createRoom(@RequestBody Map<String, String> request,
                                         @RequestParam Long userId) {
        try {
            String password = request.get("password");
            User user = userService.getUserById(userId);
            
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
            }
            
            Room room = roomService.createPrivateRoom(password, user.getUsername());
            
            // Auto join creator to room
            roomService.joinRoom(room.getRoomId(), password, userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("roomId", room.getRoomId());
            response.put("password", room.getRoomPassword());
            response.put("message", "Room created successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    // Join room with ID and password
    @PostMapping("/join")
    public ResponseEntity<?> joinRoom(@RequestBody Map<String, String> request,
                                       @RequestParam Long userId) {
        try {
            String roomId = request.get("roomId");
            String password = request.get("password");
            
            User user = userService.getUserById(userId);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
            }
            
            Room room = roomService.joinRoom(roomId, password, userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("roomId", room.getRoomId());
            response.put("message", "Joined room successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    // Get fixed room
    @GetMapping("/fixed")
    public ResponseEntity<?> getFixedRoom() {
        Room fixedRoom = roomService.getFixedRoom();
        Map<String, Object> response = new HashMap<>();
        response.put("roomId", fixedRoom.getRoomId());
        response.put("password", fixedRoom.getRoomPassword());
        return ResponseEntity.ok(response);
    }
    
    // Leave room
    @PostMapping("/leave")
    public ResponseEntity<?> leaveRoom(@RequestBody Map<String, String> request,
                                        @RequestParam Long userId) {
        try {
            String roomId = request.get("roomId");
            roomService.leaveRoom(roomId, userId);
            return ResponseEntity.ok(Map.of("message", "Left room successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
 // Send invite to user
    @PostMapping("/invite/send")
    public ResponseEntity<?> sendInvite(@RequestBody Map<String, String> request,
                                         @RequestParam Long userId) {
        try {
            String roomId = request.get("roomId");
            Long toUserId = Long.parseLong(request.get("toUserId"));
            
            // You'll need InviteService for this
            // For now, return success message
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Invite sent successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Accept invite
    @PostMapping("/invite/accept")
    public ResponseEntity<?> acceptInvite(@RequestBody Map<String, String> request,
                                           @RequestParam Long userId) {
        try {
            Long inviteId = Long.parseLong(request.get("inviteId"));
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("roomId", "temp_room_id"); // Will get from invite
            response.put("message", "Invite accepted");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Reject invite
    @PostMapping("/invite/reject")
    public ResponseEntity<?> rejectInvite(@RequestBody Map<String, String> request,
                                           @RequestParam Long userId) {
        try {
            Long inviteId = Long.parseLong(request.get("inviteId"));
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Invite rejected");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}