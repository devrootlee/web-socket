package com.websocket.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.websocket.model.SocketMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisPublisher {
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisPublisher.class);

    private final StringRedisTemplate redisTemplate;

    // WebSocket 으로 받은 메시지를 Redis 에 pub
    public void publish(String message) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            SocketMessage socketMessage = objectMapper.readValue(message, SocketMessage.class);

            // redis 전송
            redisTemplate.convertAndSend(socketMessage.getRoom(), message);

        } catch (JsonProcessingException e) {
            LOGGER.error(e.getMessage());
        }
    }
}
