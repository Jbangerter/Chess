package server.websocket;

import chess.ChessBoard;
import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.SqlDataAccess;
import io.javalin.websocket.WsCloseContext;
import io.javalin.websocket.WsCloseHandler;
import io.javalin.websocket.WsConnectContext;
import io.javalin.websocket.WsConnectHandler;
import io.javalin.websocket.WsMessageContext;
import io.javalin.websocket.WsMessageHandler;
import model.AuthData;
import org.jetbrains.annotations.NotNull;
import service.GameService;
import service.UserService;
import websocket.commands.*;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import org.eclipse.jetty.websocket.api.Session;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class WebSocketHandler implements WsConnectHandler, WsMessageHandler, WsCloseHandler {
    private final Gson gson = new Gson();
    private final ConnectionManager connectionManager = new ConnectionManager();
    ;
    private final Map<Session, Integer> sessionGameMap = new ConcurrentHashMap<>();
    private final SqlDataAccess dataAccess;


    public WebSocketHandler(SqlDataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    @Override
    public void handleConnect(@NotNull WsConnectContext ctx) {
        ctx.enableAutomaticPings();
        System.out.println("Websocket connected");
    }

    @Override
    public void handleMessage(@NotNull WsMessageContext ctx) {
        try {
            UserGameCommand command = gson.fromJson(ctx.message(), UserGameCommand.class);

            switch (command.getCommandType()) {
                case CONNECT -> connect(ctx.session, ctx.message());
                case MAKE_MOVE -> makeMove(ctx.session, ctx.message());
                case LEAVE -> leave(ctx.session, ctx.message());
                case RESIGN -> resign(ctx.session, ctx.message());
            }
        } catch (Exception e) {
            System.err.println("WebSocket Error: " + e.getMessage());
        }
    }

    @Override
    public void handleClose(@NotNull WsCloseContext ctx) {
        Integer gameID = sessionGameMap.get(ctx.session);
        if (gameID != null) {
            connectionManager.remove(gameID, ctx.session);
            sessionGameMap.remove(ctx.session);
        }
        System.out.println("Websocket closed");
    }

    private void connect(Session session, String jsonMessage) throws Exception {
        JoinGameCommand command = gson.fromJson(jsonMessage, JoinGameCommand.class);

        connectionManager.add(command.getGameID(), session);

        sessionGameMap.put(session, command.getGameID());

        //TODO: pull actual game data
        ChessGame game = new ChessGame();
        game.setBoard(new ChessBoard());
        game.getBoard().resetBoard();

        //respond to inital person
        ServerMessage loadGameMessage = new LoadGameMessage(ServerMessage.ServerMessageType.LOAD_GAME, game);
        session.getRemote().sendString(gson.toJson(loadGameMessage));


        //notify other people in game
        var notification = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, "A Thing Happened");
        connectionManager.broadcast(command.getGameID(), notification, session);
    }

    private void makeMove(Session session, String jsonMessage) throws Exception {
        MakeMoveCommand command = gson.fromJson(jsonMessage, MakeMoveCommand.class);

        // Logic: Validate move, update database game state...

        // Broadcast the new board state to EVERYONE in that game
        // (Assuming you fetch the updated game from the DB here)
        ChessGame updatedGame = new ChessGame(); // Placeholder
        LoadGameMessage loadGame = new LoadGameMessage(ServerMessage.ServerMessageType.LOAD_GAME, updatedGame);

        // Pass 'null' as the third arg if you want the person who moved to also get the update
        connectionManager.broadcast(command.getGameID(), loadGame, null);
    }

    private void leave(Session session, String jsonMessage) {
        LeaveGameCommand command = gson.fromJson(jsonMessage, LeaveGameCommand.class);
        connectionManager.remove(command.getGameID(), session);
        sessionGameMap.remove(session);
        // broadcast notification that user left...
    }

    private void resign(Session session, String jsonMessage) {
        // Resignation logic...
    }
//    @Override
//    public void handleMessage(@NotNull WsMessageContext ctx) {
//        Object message = parseFullCommand(ctx);
//
//        ctx.send(message);
//    }
//
//    @Override
//    public void handleClose(@NotNull WsCloseContext ctx) {
//        System.out.println("Websocket closed");
//    }
//
//
//
//
//
//
//    public UserGameCommand.CommandType getCommandTypeFromMessage(String jsonMessage) {
//        UserGameCommand genericCommand = gson.fromJson(jsonMessage, UserGameCommand.class);
//        return genericCommand.getCommandType();
//    }
//
//    public Object parseFullCommand(WsMessageContext ctx) {
//        UserGameCommand.CommandType type = getCommandTypeFromMessage(ctx.message());
//
//        return switch (type) {
//            case CONNECT -> connect(gson.fromJson(ctx.message(), JoinGameCommand.class));
//            case MAKE_MOVE -> gson.fromJson(ctx.message(), MakeMoveCommand.class);
//            case LEAVE -> gson.fromJson(ctx.message(), LeaveGameCommand.class);
//            case RESIGN -> gson.fromJson(ctx.message(), ReseignGameCommand.class);
//            default -> throw new IllegalArgumentException("Unknown command type: " + type);
//        };
//    }
//    //TODO: make this do the stuff handle message needs it to
//
//    private ServerMessage connect(JoinGameCommand command) {
//        System.out.println(command.toString());
//
//        ChessGame testGame = new ChessGame();
//        ChessBoard testBoard = new ChessBoard();
//        testBoard.resetBoard();
//        testGame.setBoard(testBoard);
//
//        return new LoadGameMessage(ServerMessage.ServerMessageType.LOAD_GAME, testGame);
//    }
}