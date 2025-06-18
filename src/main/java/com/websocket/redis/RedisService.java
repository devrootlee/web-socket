package com.websocket.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class RedisService {
    private final RedisTemplate<String, String> redisTemplate;

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
}
