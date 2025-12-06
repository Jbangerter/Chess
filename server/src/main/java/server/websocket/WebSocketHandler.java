package server.websocket;

import chess.ChessBoard;
import chess.ChessGame;
import com.google.gson.Gson;
import io.javalin.websocket.WsCloseContext;
import io.javalin.websocket.WsCloseHandler;
import io.javalin.websocket.WsConnectContext;
import io.javalin.websocket.WsConnectHandler;
import io.javalin.websocket.WsMessageContext;
import io.javalin.websocket.WsMessageHandler;
import org.jetbrains.annotations.NotNull;
import websocket.commands.*;
import websocket.messages.LoadGameMessage;
import websocket.messages.ServerMessage;

import java.util.Objects;

public class WebSocketHandler implements WsConnectHandler, WsMessageHandler, WsCloseHandler {
    private final Gson gson = new Gson();

    @Override
    public void handleConnect(@NotNull WsConnectContext ctx) {
        ctx.enableAutomaticPings();
        System.out.println("Websocket connected");
    }

    @Override
    public void handleMessage(@NotNull WsMessageContext ctx) {
        Object message = parseFullCommand(ctx);

        ctx.send(message);
    }

    @Override
    public void handleClose(@NotNull WsCloseContext ctx) {
        System.out.println("Websocket closed");
    }


    public UserGameCommand.CommandType getCommandTypeFromMessage(String jsonMessage) {
        UserGameCommand genericCommand = gson.fromJson(jsonMessage, UserGameCommand.class);
        return genericCommand.getCommandType();
    }

    public Object parseFullCommand(WsMessageContext ctx) {
        UserGameCommand.CommandType type = getCommandTypeFromMessage(ctx.message());

        return switch (type) {
            case CONNECT -> connect(gson.fromJson(ctx.message(), JoinGameCommand.class));
            case MAKE_MOVE -> gson.fromJson(ctx.message(), MakeMoveCommand.class);
            case LEAVE -> gson.fromJson(ctx.message(), LeaveGameCommand.class);
            case RESIGN -> gson.fromJson(ctx.message(), ReseignGameCommand.class);
            default -> throw new IllegalArgumentException("Unknown command type: " + type);
        };
    }
    //TODO: make this do the stuff handle message needs it to

    private ServerMessage connect(JoinGameCommand command) {
        System.out.println(command.toString());

        ChessGame testGame = new ChessGame();
        ChessBoard testBoard = new ChessBoard();
        testBoard.resetBoard();
        testGame.setBoard(testBoard);

        var message = new LoadGameMessage(ServerMessage.ServerMessageType.LOAD_GAME, testGame);
        return message;
    }
}