package com.websocket.handler;

import com.websocket.config.ServerInfo;
import com.websocket.service.RoomService;
import com.websocket.session.SessionManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
@RequiredArgsConstructor
public class CustomWebSocketHandler extends TextWebSocketHandler {
    private final ServerInfo serverInfo;

    private final SessionManager sessionManager;

    private final RoomService roomService;

    // [클라이언트가 websocket 연결을 맺었을 때 호출]
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        // 세션 저장
        sessionManager.register(session);

        String room = session.getAttributes().get("room").toString();
        String roomId = session.getAttributes().get("roomId").toString();
        String userId = session.getAttributes().get("userId").toString();
        String userType = session.getAttributes().get("userType").toString();

        // 교사 입장
        if (userType.equals("teacher")) {
            roomService.hostJoin(roomId, userId, userType, serverInfo.getServerId());
            return;
        }

        // 학생 입장
        if (userType.equals("student")) {
            String targetType = "teacher";
            roomService.clientJoin(room, roomId, userId, userType, serverInfo.getServerId(), session, targetType);
        }
    }

    // [클라이언트로부터 메시지를 수신했을 때 호출]
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        roomService.sendMessage(message.getPayload());
    }

    // [클라이언트가 연결을 종료했을 때 호출]
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessionManager.unRegister(session);

        String room = session.getAttributes().get("room").toString();
        String roomId = session.getAttributes().get("roomId").toString();
        String userId = session.getAttributes().get("userId").toString();
        String userType = session.getAttributes().get("userType").toString();

        // 교사 퇴장
        if (userType.equals("teacher")) {
            String targetType = "student";
            roomService.hostLeave(room, roomId, userId, userType, targetType);
        }

        // 학생 퇴장
        if (userType.equals("student")) {
            String targetType = "teacher";
            roomService.clientLeave(room, roomId, userId, userType, targetType);
        }
    }
}
