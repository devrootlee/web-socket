package com.websocket.session;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class SessionManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(SessionManager.class);

    // 세션 맵
    private final Map<String, WebSocketSession> sessionMap = new ConcurrentHashMap<>();

    // 연결된 세션을 등록
    public void register(WebSocketSession session) {
        String userId = session.getAttributes().get("userId").toString();
        sessionMap.put(userId, session);
    }

    // 연결 종료된 세션 제거
    public void unRegister(WebSocketSession session) {
        sessionMap.remove(session.getAttributes().get("userId").toString());
    }

    // 메시지를 해당 방과 타겟 역할을 가진 세션에게만 브로드캐스트
    public void broadcastToTarget(String room, String targetType, String message) {
        for (WebSocketSession session : sessionMap.values()) {
            if (!session.isOpen()) continue;

            // 세션의 userType, roomId 정보 기반 필터링
            String userType = session.getAttributes().get("userType").toString();
            String roomId = session.getAttributes().get("roomId").toString();

            if (userType != null && userType.equals(targetType) && room.equals("room:" + roomId)) {
                try {
                    session.sendMessage(new TextMessage(message));
                } catch (IOException e) {
                    LOGGER.error(e.getMessage());
                }
            }
        }
    }

}
