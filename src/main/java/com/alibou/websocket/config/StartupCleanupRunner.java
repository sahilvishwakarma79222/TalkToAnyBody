package com.alibou.websocket.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.alibou.websocket.service.RoomService;

@Component
public class StartupCleanupRunner implements ApplicationRunner {
    
    @Autowired
    private RoomService roomService;
    
    @Override
    public void run(ApplicationArguments args) throws Exception {
        System.out.println("🚀 Running startup cleanup...");
        roomService.cleanupAllStaleParticipants();
        System.out.println("✅ Startup cleanup completed!");
    }
}