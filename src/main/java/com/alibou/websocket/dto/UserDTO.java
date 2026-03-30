package com.alibou.websocket.dto;
 import lombok.Data;
import lombok.NoArgsConstructor;

import com.alibou.websocket.model.User;

import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String username;
    private String currentRoomType;
    private String currentRoomId;
    
    public static UserDTO fromUser(User user) {
        return new UserDTO(
            user.getId(),
            user.getUsername(),
            user.getCurrentRoomType(),
            user.getCurrentRoomId()
        );
    }
}