# WebSocket 기반 실시간 상호작용 서버
이 프로젝트는 **SpringBoot + WebSocket + Redis Pub/Sub** 을 기반으로 
Host 와 Client 간 실시간 상호작용이 가능하며 요구사항에 따라 구조는 얼만든지 변경 가능

---

## Skill

| 항목           | 내용                             |
|----------------|----------------------------------|
| Java           | JDK 17                           |
| Spring Boot    | 3.1.12                           |
| Redis          | 7.x               |
| Build Tool     | Gradle                           |

---
## 메시지 구조

```
{
  "room": "room:FTYXPM", - 방 ID 
  "senderType": "teacher", - 송신자 유형
  "sender": "teacher", - 송신자 ID
  "targetType": "student", - 수신자 유형
  "messageType": "send", - 메시지 유형
  "message": "여러분 안녕하세요" - 메시지
}
```

---
## WebSocket 서버 메시지 처리 과정

1. Host 가 WebSocket 서버 연결(room 생성)
2. Client 가 WebSocket 서버 연결(존재하는 room 에만 연결 가능) -> Host 한테 `join` 메시지 전달
3. Host / Client 둘 다 서로 메시지 송수신 가능
4. WebSocket 서버는 메시지를 받으면 `room:{roomId}` Redis 채널에 publish
5. 서버는 같은 room 을 subscribe 한 Client 에게만 메시지 전달
6. Client 가 WebSocket 서버 연결을 종료 -> Host 한테 `leave` 메시지 전달(참가자 관리)
7. Host 가 WebSocket 서버 연결을 종료하면 Client 들도 WebSocket 연결 종료



---

## Redis 저장 구조

| 키                             | 타입   | 설명                                    |
|--------------------------------|--------|---------------------------------------|
| `room:{roomId}`                | `Pub/Sub 채널명` | Host / Client 간 메시지 전파용               |
| `room:{roomId}:users`          | `Set`  | 해당 방에 참가 중인 userId 목록                 |
| `room:{roomId}:user:{userId}`  | `Hash` | 해당 유저의 정보 (`userType`, `serverId`) 저장 |

---

## 주요 기능 요약

- 교사/학생 각각 WebSocket으로 서버 접속
- 교사는 이미지나 메시지를 전송 가능
- 학생은 교사 메시지 실시간 수신
- 참가자 입/퇴장 시 모든 대상에 알림 전송
- Redis에 참가자 정보 저장 → REST API로 조회 가능
- 서버 장애 대비: 클라이언트 재접속 시 고아 세션 정리 로직 포함

---

## 웹소캣 서버 접속 순서

```
[클라이언트 브라우저]
        |
        | 1. 로그인 (HTTP API)
        v
[서비스 서버 (Spring Boot REST API)]
- /login → JWT 발급
- /profile → 유저 정보
- DB 접근 및 권한 관리
        |
        | 2. 받은 JWT를 WebSocket 연결 시 사용
        v
[WebSocket 서버]
- 클라이언트가 JWT 포함해 연결 시도
- JWT 검증 후 연결 허용 / 차단
```



---

## 프로젝트 구조
```
src
├── java
│   └── com
│       └── websocket
│           ├── Application.java - 메인
│           ├── config - 설정파일
│           │   ├── CustomHandShakeInterceptor.java - 클라이언트와 웹소캣 연결 직전의 핸드세이크
│           │   ├── RedisConfig.java - 레디스 설정
│           │   ├── ServerInfo.java - 서버 ID 생성
│           │   └── WebSocketConfig.java - 웹소캣 설정
│           ├── handler
│           │   └── CustomWebSocketHandler.java - 웹소캣 연결 이후의 메시지 송수신 처리
│           ├── model
│           │   └── SocketMessage.java - 웹소캣 메시지 정의
│           ├── redis
│           │   ├── RedisPublisher.java - 웹소캣에서 받은 메시지를 레디스에 publish 
│           │   ├── RedisService.java - 도메인 로직 처리 (방 입장/퇴장, 메시지 발행 등)
│           │   └── RedisSubscriber.java - 레디스로부터 받은 메시지를 subscribe
│           ├── service
│           │   └── RoomService.java - redisServie 호출
│           └── session
│               └── SessionManager.java - 서버에 연결된 세션 관리
└── resources
    ├── application.yml
    └── static
        ├── student.html
        └── teacher.html
```

---
## 테스트방법
1. 접속: ssh -i [pemkey] ubuntu@[hostIP]
2. 레디스 도커 접근: sudo docker exec -it redis redis-cli
3. 레디스 모니터링: monitor
4. 레디스 명령어
   - KEYS *: 레디스에 저장되어있는 모든 KEY 확인
   - HGETALL [key]: 해당 key 에 있는 hashkey 확인
   - 
