package com.websocket.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocketMessage {
    private String room;
    private String sender;
    private String senderType;
    private String targetType;
    private String messageType;
    private Object message;
}
