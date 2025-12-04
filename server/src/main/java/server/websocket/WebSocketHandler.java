package server.websocket;

import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;

@ServerEndpoint("/ws")
public class WebSocketHandler {

    @OnOpen
    public void onOpen(Session session) {
        System.out.println("Server: New client connected. ID: " + session.getId());
    }

    @OnMessage
    public String onMessage(String message, Session session) {
        System.out.println("Server received PING: " + message);

        return "Server echoed: " + message;
    }

    @OnClose
    public void onClose(Session session) {
        System.out.println("Server: Client disconnected. ID: " + session.getId());
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.err.println("Server error for session " + session.getId() + ": " + throwable.getMessage());
    }
}