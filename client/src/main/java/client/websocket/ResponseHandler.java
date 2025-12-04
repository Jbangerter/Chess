package client.websocket;

import websocket.messages.ServerMessage;

public interface ResponseHandler {
    void notify(ServerMessage message);
}