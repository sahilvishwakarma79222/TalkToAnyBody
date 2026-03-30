package com.alibou.websocket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ChatApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChatApplication.class, args);
		 System.out.println("🚀 Chat Application Started!");
	        System.out.println("📍 H2 Console: http://localhost:8082/h2-console");
	        System.out.println("📍 App URL: http://localhost:8082");
	}

}
