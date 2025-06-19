# WebSocket 서버

### 웹소캣 서버 접속 순서
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
### 폴더 구조 및 설명
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


### 테스트방법
1. 접속: ssh -i [pemkey] ubuntu@[hostIP]
2. 레디스 도커 접근: sudo docker exec -it redis redis-cli
3. 레디스 모니터링: monitor
