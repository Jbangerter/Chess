package service.GameServiceRecords;

import chess.ChessGame;

public record JoinGameInput(ChessGame.TeamColor playerColor, int gameID) {
}
