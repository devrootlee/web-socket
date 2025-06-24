package com.websocket.controller;

import com.websocket.redis.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/monitoring")
@RequiredArgsConstructor
public class MonitoringController {
    private final RedisService redisService;

    @GetMapping("/users")
    public List<Map<String, String>> getAllUsers() {
        return redisService.getAllConnectedUsers();
    }
}
