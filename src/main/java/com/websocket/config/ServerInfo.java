package com.websocket.config;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ServerInfo {
    private final String serverId = UUID.randomUUID().toString();

    public String getServerId() {
        return serverId;
    }
}
