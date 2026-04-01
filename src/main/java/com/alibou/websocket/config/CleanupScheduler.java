package com.alibou.websocket.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.alibou.websocket.service.RoomService;
import com.alibou.websocket.service.UserService;

@Component
@EnableScheduling
public class CleanupScheduler {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private RoomService roomService;
    
    // ✅ Run every 5 minutes
    @Scheduled(fixedRate = 300000)
    public void cleanupInactiveUsers() {
        System.out.println("🕐 Running scheduled cleanup...");
        userService.cleanupInactiveUsers();
        roomService.cleanupAllStaleParticipants();
        System.out.println("✅ Scheduled cleanup completed");
    }
}