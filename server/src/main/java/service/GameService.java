package service;

import Exceptions.*;
import chess.ChessGame;
import dataaccess.MemoryDataAccess;
import model.*;

import java.util.Objects;

public class GameService {

    private final MemoryDataAccess dataAccess;

    public GameService(MemoryDataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public GameData[] listGames(String authToken) {
        if (!dataAccess.validateAuthToken(authToken)) {
            throw new UnauthorizedException("Error: unauthorized");
        }

        return dataAccess.listGames();
    }

    public int createGame(String authToken, String gameName) {
        if (!dataAccess.validateAuthToken(authToken)) {
            throw new UnauthorizedException("Error: unauthorized");
        }
        if (gameName == null || gameName.isEmpty()) {
            throw new BadRequestException("Error: bad request");
        }


        int gameID = dataAccess.numGames() + 1;
        dataAccess.createGame(new GameData(gameID, "", "", gameName, new ChessGame()));

        return gameID;
    }


    public void joinGame(String authToken, ChessGame.TeamColor playerColor, int gameID) {
        if (!dataAccess.validateAuthToken(authToken)) {
            throw new UnauthorizedException("Error: unauthorized");
        }
        if (playerColor == null) {
            throw new BadRequestException("Error: bad request");
        }
        if (!dataAccess.gameIDExists(gameID)) {
            throw new BadRequestException("Error: bad request");
        }

        GameData game = dataAccess.getGame(gameID);
        var userAuthdata = dataAccess.getAuthdataFromAuthtoken(authToken);
        var userData = dataAccess.getUser(userAuthdata.username());
        GameData updatedGame = game;


        if (playerColor == ChessGame.TeamColor.BLACK) {
            if (!Objects.equals(game.blackUsername(), "")) {
                throw new AlreadyTakenException("Error: already taken");
            }

            updatedGame = new GameData(game.gameID(), game.whiteUsername(), userData.username(), game.gameName(), game.game());

        } else if (playerColor == ChessGame.TeamColor.WHITE) {
            if (!Objects.equals(game.whiteUsername(), "")) {
                throw new AlreadyTakenException("Error: already taken");
            }

            updatedGame = new GameData(game.gameID(), userData.username(), game.blackUsername(), game.gameName(), game.game());
        } else {
            throw new BadRequestException("Error: bad request");
        }

        dataAccess.updateGame(updatedGame);

    }

}
