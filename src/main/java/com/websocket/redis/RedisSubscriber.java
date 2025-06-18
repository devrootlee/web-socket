package com.websocket.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.websocket.model.SocketMessage;
import com.websocket.session.SessionManager;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class RedisSubscriber implements MessageListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisSubscriber.class);

    private final SessionManager sessionManager;

    // Redis 로부터 메시지를 수신했을 때 호출
    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String payload = new String(message.getBody(), StandardCharsets.UTF_8);
            ObjectMapper objectMapper = new ObjectMapper();
            SocketMessage socketMessage = objectMapper.readValue(payload, SocketMessage.class);

            sessionManager.broadcastToTarget(socketMessage.getRoom(), socketMessage.getTargetType(), payload);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
    }
}
