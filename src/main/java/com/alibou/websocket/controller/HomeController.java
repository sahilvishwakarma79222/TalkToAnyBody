package com.alibou.websocket.controller;

  import jakarta.servlet.http.HttpSession;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.alibou.websocket.dto.MessageDTO;
import com.alibou.websocket.dto.UserDTO;
import com.alibou.websocket.model.Message;
import com.alibou.websocket.model.User;
import com.alibou.websocket.service.ChatService;
import com.alibou.websocket.service.UserService;

@Controller
public class HomeController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private ChatService chatService;
    
    // Welcome page
    @GetMapping("/")
    public String welcomePage(HttpSession session, Model model) {
        if (session.getAttribute("userId") != null) {
            return "redirect:/chat";
        }
        return "index";
    }
    
    // Join chat - process name and create user
    @PostMapping("/join")
    public String joinChat(@RequestParam String username, 
                           HttpSession session, 
                           Model model) {
        if (username == null || username.trim().isEmpty()) {
            model.addAttribute("error", "Please enter a valid name");
            return "index";
        }
        
        User user = userService.createUser(username.trim(), session.getId());
        
        session.setAttribute("userId", user.getId());
        session.setAttribute("username", user.getUsername());
        session.setAttribute("sessionId", session.getId());
        
        return "redirect:/chat";
    }
    
    // Main chat page
    @GetMapping("/chat")
    public String chatPage(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/";
        }
        
        String username = (String) session.getAttribute("username");
        model.addAttribute("username", username);
        model.addAttribute("userId", userId);
        
        return "chat";
    }
    
    // Get recent messages
    @GetMapping("/api/messages/recent")
    @ResponseBody
    public List<MessageDTO> getRecentMessages(@RequestParam(defaultValue = "GLOBAL") String roomType,
                                               @RequestParam(required = false) String roomId,
                                               @RequestParam(defaultValue = "50") int limit) {
        List<Message> messages = chatService.getRecentMessages(roomType, roomId, limit);
        return messages.stream()
                .map(MessageDTO::fromMessage)
                .collect(Collectors.toList());
    }
    
    // Logout
    @PostMapping("/logout")
    public String logout(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId != null) {
            userService.removeUser(userId);
        }
        session.invalidate();
        return "redirect:/";
    }
    
    
    @GetMapping("/api/users/online")
    @ResponseBody
    public List<UserDTO> getOnlineUsers() {
        List<User> users = userService.getUsersInGlobalChat();
        return users.stream()
                .map(UserDTO::fromUser)
                .collect(Collectors.toList());
    }
}