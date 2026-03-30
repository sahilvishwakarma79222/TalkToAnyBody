package com.alibou.websocket.dto;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomDTO {
    private String roomId;
    private String createdBy;
    private Boolean isFixed;
    private Integer participantCount;
    private LocalDateTime createdAt;
}