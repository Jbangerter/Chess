package server.websocket;

import websocket.messages.ServerMessage;

public interface MessageObserver {
    void notify(ServerMessage message);
}
