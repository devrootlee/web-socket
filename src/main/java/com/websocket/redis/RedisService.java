package com.websocket.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.websocket.model.SocketMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class RedisService {
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisService.class);

    private final RedisTemplate<String, String> redisTemplate;
    private final RedisPublisher redisPublisher;

    // 방 생성
    public void createRoom(String roomId, String userId, String userType, String serverId) {
        addUser(roomId, userId, userType, serverId);
    }

    // 사용자 추가
    public void addUser(String roomId, String userId, String userType, String serverId) {
        // 방에 사용자 인덱스 저장
        String redisKey = "room:" + roomId + ":users";
        redisTemplate.opsForSet().add(redisKey, userId);

        // 사용자 정보 저장
        String hashKey = "room:" + roomId + ":user:" + userId;
        redisTemplate.opsForHash().put(hashKey, "userType", userType);
        redisTemplate.opsForHash().put(hashKey, "serverId", serverId);
    }

    // 사용자 삭제
    public void removeUser(String roomId, String userId) {
        // 방에서 사용자 인덱스에서 userId 제거
        String redisKey = "room:" + roomId + ":users";
        redisTemplate.opsForSet().remove(redisKey, userId);

        // 사용자 정보 삭제
        String hashKey = "room:" + roomId + ":user:" + userId;
        redisTemplate.delete(hashKey);
    }

    // 방에 교사 존재 여부 확인
    public boolean hostInRoom(String roomId) {
        String redisKey = "room:" + roomId + ":users";
        Set<String> users = redisTemplate.opsForSet().members(redisKey);

        if (users == null) return false;

        for (String userId : users) {
            String hashKey = "room:" + roomId + ":user:" + userId;
            String userType = (String) redisTemplate.opsForHash().get(hashKey, "userType");
            if ("teacher".equals(userType)) return true;
        }
        return false;
    }

    // 방 삭제
    public void deleteRoom(String roomId) {
        String redisKey = "room:" + roomId + ":users";
        Set<String> users = redisTemplate.opsForSet().members(redisKey);

        if (users != null) {
            for (String userId : users) {
                String hashKey = "room:" + roomId + ":user:" + userId;
                redisTemplate.delete(hashKey);
            }
        }
        redisTemplate.delete(redisKey);
    }

    // 전체 사용자 조회
    public List<Map<String, String>> getAllConnectedUsers() {
        List<Map<String, String>> result = new ArrayList<>();

        Set<String> roomKeys = redisTemplate.keys("room:*:users");

        if (roomKeys == null) return result;

        for (String roomKey : roomKeys) {
            String roomId = roomKey.split(":")[1];

            Set<String> userIds = redisTemplate.opsForSet().members(roomKey);

            if (userIds == null) continue;

            for (String userId : userIds) {
                String userKey = "room:" + roomId + ":user:" + userId;

                String userType = (String) redisTemplate.opsForHash().get(userKey, "userType");
                String serverId = (String) redisTemplate.opsForHash().get(userKey, "serverId");

                Map<String, String> userMap = new HashMap<>();
                userMap.put("roomId", roomId);
                userMap.put("userId", userId);
                userMap.put("userType", userType);
                userMap.put("serverId", serverId);

                result.add(userMap);
            }
        }

        return result;
    }

    // 메시지 전송
    public void publish(SocketMessage message) {
        try {
            redisPublisher.publish(new ObjectMapper().writeValueAsString(message));
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
    }
}
