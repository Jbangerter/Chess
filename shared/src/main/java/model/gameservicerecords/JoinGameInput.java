package model.gameservicerecords;

import chess.ChessGame;

public record JoinGameInput(ChessGame.TeamColor playerColor, int gameID, boolean observer) {
    public JoinGameInput(ChessGame.TeamColor playerColor, int gameID){
        this(playerColor, gameID, false);
    }

}
