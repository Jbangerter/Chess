package server.websocket;

import chess.ChessGame;
import chess.ChessMove;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import dataaccess.SqlDataAccess;
import io.javalin.websocket.WsCloseContext;
import io.javalin.websocket.WsCloseHandler;
import io.javalin.websocket.WsConnectContext;
import io.javalin.websocket.WsConnectHandler;
import io.javalin.websocket.WsMessageContext;
import io.javalin.websocket.WsMessageHandler;
import model.GameData;
import model.UserData;
import org.jetbrains.annotations.NotNull;
import service.GameService;
import service.UserService;
import websocket.commands.*;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import org.eclipse.jetty.websocket.api.Session;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class WebSocketHandler implements WsConnectHandler, WsMessageHandler, WsCloseHandler {
    private final Gson gson = new Gson();
    private final ConnectionManager connectionManager = new ConnectionManager();

    private final Map<Session, Integer> sessionGameMap = new ConcurrentHashMap<>();

    private UserService userService;
    private GameService gameService;
    private final SqlDataAccess dataAccess;

    public WebSocketHandler(UserService userService, GameService gameService, SqlDataAccess dataAccess) {
        this.userService = userService;
        this.gameService = gameService;
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

        try {
            verifyInput(session, command);
        } catch (Exception e) {
            ServerMessage errorMessage = new ErrorMessage(ServerMessage.ServerMessageType.ERROR, e.getMessage());
            session.getRemote().sendString(gson.toJson(errorMessage));
            throw new InvalidMoveException(e.getMessage());
        }

        connectionManager.add(command.getGameID(), session);
        sessionGameMap.put(session, command.getGameID());

        var game = dataAccess.getGame(command.getGameID());

        var user = dataAccess.getUser(dataAccess.getAuthdataFromAuthtoken(command.getAuthToken()).username());

        //respond to inital person
        ServerMessage loadGameMessage = new LoadGameMessage(ServerMessage.ServerMessageType.LOAD_GAME, game);
        session.getRemote().sendString(gson.toJson(loadGameMessage));

        //notify other people in game
        var notification = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, user.username() + " joined as " + command.getRole());
        connectionManager.broadcast(command.getGameID(), notification, session);
    }

    private void makeMove(Session session, String jsonMessage) throws Exception {
        MakeMoveCommand command = gson.fromJson(jsonMessage, MakeMoveCommand.class);

        try {
            verifyInput(session, command);
        } catch (Exception e) {
            ServerMessage errorMessage = new ErrorMessage(ServerMessage.ServerMessageType.ERROR, e.getMessage());
            session.getRemote().sendString(gson.toJson(errorMessage));
            throw new InvalidMoveException(e.getMessage());
        }

        var gameData = dataAccess.getGame(command.getGameID());
        var user = dataAccess.getUser(dataAccess.getAuthdataFromAuthtoken(command.getAuthToken()).username());

        try {
            validateMove(gameData, user, command.getMove());
        } catch (InvalidMoveException e) {
            ServerMessage moveErrorMessage = new ErrorMessage(ServerMessage.ServerMessageType.ERROR, e.getMessage());
            session.getRemote().sendString(gson.toJson(moveErrorMessage));
            throw new InvalidMoveException(e.getMessage());
        }

        announceGameStatus(command, gameData, session);
        dataAccess.updateGame(gameData);
    }

    private void announceGameStatus(MakeMoveCommand command, GameData gameData, Session session) throws IOException, DataAccessException {
        var game = gameData.game();
        var user = dataAccess.getUser(dataAccess.getAuthdataFromAuthtoken(command.getAuthToken()).username());
        var blackUsername = gameData.blackUsername();
        var whiteUsername = gameData.whiteUsername();
        var moveMessage = user.username() + " moved: " + command.getMove().toString() + "\n";


        LoadGameMessage loadGame = new LoadGameMessage(ServerMessage.ServerMessageType.LOAD_GAME, gameData);
        connectionManager.broadcast(command.getGameID(), loadGame, null);


        if (game.isInCheck(ChessGame.TeamColor.BLACK)) {
            if (game.isInCheckmate(ChessGame.TeamColor.BLACK)) {
                var notification = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, moveMessage + blackUsername + " is in checkmate, " + whiteUsername + " wins.");
                connectionManager.broadcast(command.getGameID(), notification, session);
                game.setGameOver();
            } else {
                var notification = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, moveMessage + blackUsername + " is in check");
                connectionManager.broadcast(command.getGameID(), notification, session);
            }
        } else if (game.isInCheck(ChessGame.TeamColor.WHITE)) {
            if (game.isInCheckmate(ChessGame.TeamColor.WHITE)) {
                var notification = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, moveMessage + whiteUsername + " is in checkmate, " + blackUsername + " wins.");
                connectionManager.broadcast(command.getGameID(), notification, session);
                game.setGameOver();
            } else {
                var notification = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, moveMessage + whiteUsername + " is in check");
                connectionManager.broadcast(command.getGameID(), notification, session);
            }
        } else if (game.isInStalemate(ChessGame.TeamColor.BLACK) || game.isInStalemate(ChessGame.TeamColor.WHITE)) {
            var notification = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, moveMessage + "The game is a stalemate and has ended in a draw");
            connectionManager.broadcast(command.getGameID(), notification, session);
            game.setGameOver();
        } else {
            var moveNotification = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, moveMessage);
            connectionManager.broadcast(command.getGameID(), moveNotification, session);
        }


    }

    private void validateMove(GameData gameData, UserData user, ChessMove move) throws InvalidMoveException {
        ChessGame.TeamColor movedPieceColor = gameData.game().getBoard().getPiece(move.getStartPosition()).getTeamColor();
        ChessGame game = gameData.game();
        ChessGame gameAfterMove = game.deepCopy();


        if (game.isGameOver()) {
            throw new InvalidMoveException("Game is over, No further moves may be played");
        }
        if (movedPieceColor == ChessGame.TeamColor.BLACK) {
            if (!Objects.equals(user.username(), gameData.blackUsername())) {
                throw new InvalidMoveException(move + " Targets a " + movedPieceColor + " piece.");
            }
        }
        if (movedPieceColor == ChessGame.TeamColor.WHITE) {
            if (!Objects.equals(user.username(), gameData.whiteUsername())) {
                throw new InvalidMoveException(move + " Targets a " + movedPieceColor + " piece.");
            }
        }

        gameAfterMove.makeMove(move);

        if (gameAfterMove.isInCheck(movedPieceColor)) {
            throw new InvalidMoveException(move + "places you in check");
        }

        game.makeMove(move);
    }

    private void leave(Session session, String jsonMessage) throws Exception {
        LeaveGameCommand command = gson.fromJson(jsonMessage, LeaveGameCommand.class);

        try {
            verifyInput(session, command);
        } catch (Exception e) {
            ServerMessage leaveErrorMessage = new ErrorMessage(ServerMessage.ServerMessageType.ERROR, e.getMessage());
            session.getRemote().sendString(gson.toJson(leaveErrorMessage));
            throw new InvalidMoveException(e.getMessage());
        }

        var user = dataAccess.getUser(dataAccess.getAuthdataFromAuthtoken(command.getAuthToken()).username());
        var gameData = dataAccess.getGame(command.getGameID());
        GameData newGameData;

        if (Objects.equals(gameData.blackUsername(), user.username())) {
            newGameData = new GameData(gameData.gameID(), gameData.whiteUsername(), null, gameData.gameName(), gameData.game());
        } else if (Objects.equals(gameData.whiteUsername(), user.username())) {
            newGameData = new GameData(gameData.gameID(), null, gameData.blackUsername(), gameData.gameName(), gameData.game());
        } else {
            newGameData = gameData;
        }


        dataAccess.updateGame(newGameData);

        connectionManager.remove(command.getGameID(), session);
        sessionGameMap.remove(session);

        var notification = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, user.username() + "has left the game");
        connectionManager.broadcast(command.getGameID(), notification, session);

    }

    private void resign(Session session, String jsonMessage) throws Exception {
        ReseignGameCommand command = gson.fromJson(jsonMessage, ReseignGameCommand.class);

        try {
            verifyInput(session, command);
        } catch (Exception e) {
            ServerMessage resignErrorMessage = new ErrorMessage(ServerMessage.ServerMessageType.ERROR, e.getMessage());
            session.getRemote().sendString(gson.toJson(resignErrorMessage));
            throw new InvalidMoveException(e.getMessage());
        }


        var user = dataAccess.getUser(dataAccess.getAuthdataFromAuthtoken(command.getAuthToken()).username());
        var gameData = dataAccess.getGame(command.getGameID());


        if (gameData.game().isGameOver()) {
            ServerMessage errorMessage = new ErrorMessage(ServerMessage.ServerMessageType.ERROR, "The game is over, you cannot resign.");
            session.getRemote().sendString(gson.toJson(errorMessage));

        } else if (Objects.equals(user.username(), gameData.whiteUsername()) || Objects.equals(user.username(), gameData.blackUsername())) {
            var notification = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, user.username() + "has resigned the game");
            connectionManager.broadcast(command.getGameID(), notification, null);

            gameData.game().setGameOver();
            dataAccess.updateGame(gameData);

            connectionManager.remove(command.getGameID(), session);
            sessionGameMap.remove(session);
        } else {
            ServerMessage errorMessage = new ErrorMessage(ServerMessage.ServerMessageType.ERROR, "You are an observer and cannot resign, if you want to leave use the leave command");
            session.getRemote().sendString(gson.toJson(errorMessage));

            connectionManager.remove(command.getGameID(), session);
            sessionGameMap.remove(session);
        }


    }


    private void verifyInput(Session session, UserGameCommand command) throws Exception {
        if (!dataAccess.authTokenExists(command.getAuthToken())) {
            throw new Exception("Error: Unauthorized user data please login again");
        }
        if (!dataAccess.gameIDExists(command.getGameID())) {
            throw new Exception("Error: Invalid Game ID");
        }

    }
}