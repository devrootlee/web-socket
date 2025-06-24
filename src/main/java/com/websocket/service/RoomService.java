package com.websocket.service;

import com.websocket.model.SocketMessage;
import com.websocket.redis.RedisPublisher;
import com.websocket.redis.RedisService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class RoomService {
    private static final Logger LOGGER = LoggerFactory.getLogger(RoomService.class);

    private final RedisPublisher redisPublisher;
    private final RedisService redisService;

    public void hostJoin(String roomId, String userId, String userType, String serverId) {
        redisService.createRoom(roomId, userId, userType, serverId);
    }

    public void clientJoin(String room, String roomId, String userId, String userType, String serverId, WebSocketSession session, String targetType) {
        if (!redisService.hostInRoom(roomId)) {
            try {
                session.sendMessage(new TextMessage("교사가 없어 접속이 불가합니다."));
                session.close();
                return;
            } catch (IOException e) {
                LOGGER.error(e.getMessage());
            }
        }

        redisService.addUser(roomId, userId, userType, serverId);

        // host 한테 메시지 보내기
        SocketMessage message = SocketMessage.builder()
                .room(room)
                .sender(userId)
                .senderType(userType)
                .targetType(targetType)
                .messageType("join")
                .message(userId + "입장")
                .build();

        redisService.publish(message);
    }

    public void sendMessage(String payload) {
        redisPublisher.publish(payload);
    }

    public void hostLeave(String room, String roomId, String userId, String userType, String targetType) {
        redisService.deleteRoom(roomId);

        // client 한테 메시지 보내기
        SocketMessage message = SocketMessage.builder()
                .room(room)
                .senderType(userType)
                .sender(userId)
                .targetType(targetType)
                .messageType("leave")
                .message(userId + "퇴장")
                .build();

        redisService.publish(message);
    }

    public void clientLeave(String room, String roomId, String userId, String userType, String targetType) {
        redisService.removeUser(roomId, userId);

        // host 한테 메시지 보내기
        SocketMessage message = SocketMessage.builder()
                .room(room)
                .senderType(userType)
                .sender(userId)
                .targetType(targetType)
                .messageType("leave")
                .message(userId + "퇴장")
                .build();

        redisService.publish(message);
    }
}
