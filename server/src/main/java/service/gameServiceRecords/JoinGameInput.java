package service.gameServiceRecords;

import chess.ChessGame;

public record JoinGameInput(ChessGame.TeamColor playerColor, int gameID) {
}
