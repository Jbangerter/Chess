package service.gameServiceRecords;

import chess.ChessGame;

public record joinGameInput(ChessGame.TeamColor playerColor, int gameID) {
}
