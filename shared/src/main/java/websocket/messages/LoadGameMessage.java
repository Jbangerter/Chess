package websocket.messages;

import chess.ChessGame;

public class LoadGameMessage extends ServerMessage {

    ChessGame game;

    public LoadGameMessage(ServerMessageType type, ChessGame inputGame) {
        super(type);
        game = inputGame;
    }

}
