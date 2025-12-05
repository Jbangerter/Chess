package client.websocket;

import com.google.gson.Gson;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.Endpoint;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.MessageHandler;
import jakarta.websocket.Session;
import jakarta.websocket.WebSocketContainer;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.net.URI;
import java.util.Scanner;

public class WebSocketFacade extends Endpoint {
    public Session session;

    public static void main(String[] args) throws Exception {
        WebSocketFacade client = new WebSocketFacade("ws://localhost:8080/ws");

        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter a message you want to echo:");
        while (true) {
            client.send(scanner.nextLine());
        }
    }

    public WebSocketFacade(String webSocketUri) throws Exception {
        URI uri = new URI(webSocketUri);
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        session = container.connectToServer(this, uri);

        this.session.addMessageHandler(new MessageHandler.Whole<String>() {
            public void onMessage(String message) {
//                ServerMessage notification = new Gson().fromJson(message, ServerMessage.class);
//                ResponseHandler.notify(notification);
                //TODO: Make this actualy process the various kinds of messages and handle those properly

                System.out.println(message);
            }
        });
    }

    public void send(String message) throws IOException {
        session.getBasicRemote().sendText(message);
    }

    public void ping(String message) throws IOException {
        session.getBasicRemote().sendText(message);
    }


    // This method must be overridden, but we don't have to do anything with it
    public void onOpen(Session session, EndpointConfig endpointConfig) {
    }
}