package client.websocket;

import org.glassfish.tyrus.client.ClientManager;
import jakarta.websocket.*;

import java.net.URI;

public class WebSocketFacade extends Endpoint {

    private Session session;

    public WebSocketFacade(String url) {
        try {
            URI socketURI = new URI(url.replace("http", "ws") + "/ws");
            ClientManager client = ClientManager.createClient();

            this.session = client.connectToServer(this, socketURI);

            this.session.addMessageHandler(new MessageHandler.Whole<String>() {
                @Override
                public void onMessage(String message) {
                    System.out.println("Client received PONG: " + message);
                }
            });

        } catch (Exception ex) {
            System.err.println("Connection failed. Did the server start? Error: " + ex.getMessage());
        }
    }

    public void sendPing(String text) {
        try {
            System.out.println("Client sending PING: " + text);
            this.session.getBasicRemote().sendText(text);
        } catch (Exception ex) {
            System.err.println("Failed to send message: " + ex.getMessage());
        }
    }

    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
        System.out.println("Client: WebSocket connection established.");
    }

}