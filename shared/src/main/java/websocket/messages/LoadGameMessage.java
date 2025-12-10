package websocket.messages;

import chess.ChessGame;
import chess.ChessMove;
import model.GameData;

public class LoadGameMessage extends ServerMessage {

    ChessGame game;

    public LoadGameMessage(ServerMessageType type, GameData inputGame) {
        super(type);
        game = inputGame.game();
    }

}
