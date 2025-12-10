package client.websocket;

import chess.ChessGame;
import chess.ChessMove;
import com.google.gson.Gson;
import jakarta.websocket.*;
import websocket.commands.*;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.net.URI;

@ClientEndpoint
public class WebSocketFacade {

    private Session session;
    private MessageObserver observer;
    private Gson gson;

    public WebSocketFacade(String url, MessageObserver observer) throws DeploymentException, IOException {
        this.observer = observer;
        this.gson = new Gson();

        URI uri = URI.create(url);
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();

        this.session = container.connectToServer(this, uri);
    }

    @OnMessage
    public void onMessage(String message) {
        try {
            // Deserialize the JSON string into a ServerMessage object
            ServerMessage serverMessage = gson.fromJson(message, ServerMessage.class);

            // Pass the message to observer
            observer.notify(serverMessage);

        } catch (Exception e) {
            System.err.println("Failed to handle message: " + e.getMessage());
        }
    }

//    @OnOpen
//    public void onOpen(Session session) {
//         System.out.println("Connected to WebSocket server");
//    }

//    @OnError
//    public void onError(Session session, Throwable throwable) {
//        System.err.println("WebSocket error: " + throwable.getMessage());
//    }


    private void sendCommand(UserGameCommand command) throws IOException {
        if (session != null && session.isOpen()) {
            String json = gson.toJson(command);
            session.getBasicRemote().sendText(json);
        } else {
            throw new IOException("Connection is closed");
        }
    }


    public void joinPlayer(String authToken, int gameID, ChessGame.TeamColor playerColor) throws IOException {
        JoinGameCommand command = new JoinGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameID, playerColor.toString());
        sendCommand(command);
    }

    public void joinObserver(String authToken, int gameID) throws IOException {
        JoinGameCommand command = new JoinGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameID, "OBSERVER");
        sendCommand(command);
    }

    public void makeMove(String authToken, int gameID, ChessMove move) throws IOException {
        MakeMoveCommand command = new MakeMoveCommand(UserGameCommand.CommandType.MAKE_MOVE, authToken, gameID, move);
        sendCommand(command);
    }

    public void leave(String authToken, int gameID) throws IOException {
        LeaveGameCommand command = new LeaveGameCommand(UserGameCommand.CommandType.LEAVE, authToken, gameID);
        sendCommand(command);
    }

    public void resign(String authToken, int gameID) throws IOException {
        ReseignGameCommand command = new ReseignGameCommand(UserGameCommand.CommandType.RESIGN, authToken, gameID);
        sendCommand(command);
    }
}

//
//public class WebSocketFacade extends Endpoint {
//    public Session session;
//
//    public static void main(String[] args) throws Exception {
//        WebSocketFacade client = new WebSocketFacade("ws://localhost:8080/ws");
//
//        Scanner scanner = new Scanner(System.in);
//
//        System.out.println("Enter a message you want to echo:");
//        while (true) {
//            client.send(scanner.nextLine());
//        }
//    }
//
//    public WebSocketFacade(String webSocketUri) throws Exception {
//        URI uri = new URI(webSocketUri);
//        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
//        session = container.connectToServer(this, uri);
//
//        this.session.addMessageHandler(new MessageHandler.Whole<String>() {
//            public void onMessage(String message) {
/// /                ServerMessage notification = new Gson().fromJson(message, ServerMessage.class);
/// /                ResponseHandler.notify(notification);
//                //TODO: Make this actualy process the various kinds of messages and handle those properly
//
//                System.out.println(message);
//            }
//        });
//    }
//
//    public void send(String message) throws IOException {
//        session.getBasicRemote().sendText(message);
//    }
//
//    public void ping(String message) throws IOException {
//        session.getBasicRemote().sendText(message);
//    }
//
//
//    // This method must be overridden, but we don't have to do anything with it
//    public void onOpen(Session session, EndpointConfig endpointConfig) {
//    }
//}