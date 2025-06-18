package com.websocket.config;


import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

// WebSocket 연결 시 값 저장
public class CustomHandShakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {

        if (request instanceof ServletServerHttpRequest servletServerHttpRequest) {
            HttpServletRequest servletRequest = servletServerHttpRequest.getServletRequest();

            // 모든 쿼리 파라미터를 attributes에 복사
            servletRequest.getParameterMap().forEach((key, values) -> {
                if (values != null && values.length > 0) {
                    attributes.put(key, values[0]); // 첫 번째 값만 사용
                }
            });

            // validate
            if (!attributes.containsKey("userId") ||
                    !attributes.containsKey("userType") ||
                    !attributes.containsKey("roomId") ) {
                response.setStatusCode(HttpStatus.BAD_REQUEST);
                return false;
            }

            // room 값 넣기
            String roomId = attributes.get("roomId").toString();
            attributes.put("room", "room:" + roomId);
        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {

    }
}
