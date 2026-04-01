package com.alibou.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.alibou.websocket.service.RoomService;

@SpringBootApplication
@EnableScheduling
public class ChatApplication implements ApplicationRunner {

	   @Autowired
	    private RoomService roomService;
	   
	public static void main(String[] args) {
		SpringApplication.run(ChatApplication.class, args);
		
	}

	
	 @Override
	    public void run(ApplicationArguments args) throws Exception {
	        System.out.println("🚀 Startup cleanup skipped (temporarily disabled)");
	        // Comment out or remove the cleanup call
	        // roomService.cleanupAllStaleParticipants();
	    }

}
