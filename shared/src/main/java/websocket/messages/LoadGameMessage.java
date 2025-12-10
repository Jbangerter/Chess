package websocket.messages;

import chess.ChessGame;
import chess.ChessMove;
import model.GameData;

public class LoadGameMessage extends ServerMessage {

    private ChessGame game;

    public LoadGameMessage(ServerMessageType type, GameData inputGame) {
        super(type);
        game = inputGame.game();
    }

    public ChessGame getGame() {
        return game;
    }

    public void setGame(ChessGame game) {
        this.game = game;
    }
}
