package client.websocket;

import websocket.messages.ServerMessage;

public interface MessageObserver {
    void notify(String message);
}
