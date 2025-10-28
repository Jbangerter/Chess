package service;

import exceptions.*;
import chess.ChessGame;
import dataaccess.MemoryDataAccess;
import model.*;
import service.gameServiceRecords.gameListData;
import service.gameServiceRecords.shortenedGameData;

import java.util.ArrayList;
import java.util.Collection;

public class GameService {

    private final MemoryDataAccess dataAccess;

    public GameService(MemoryDataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public gameListData listGames(String authToken) {
        if (!dataAccess.validateAuthToken(authToken)) {
            throw new UnauthorizedException("Error: unauthorized");
        }

        Collection<shortenedGameData> gameList = new ArrayList<>();

        Collection<GameData> unprocessedGames = dataAccess.listGames();

        for (GameData game : unprocessedGames) {
            gameList.add(new shortenedGameData(game.gameID(), game.whiteUsername(), game.blackUsername(), game.gameName()));
        }

        return new gameListData(gameList);
    }

    public int createGame(String authToken, String gameName) {
        if (!dataAccess.validateAuthToken(authToken)) {
            throw new UnauthorizedException("Error: unauthorized");
        }
        if (gameName == null || gameName.isEmpty()) {
            throw new BadRequestException("Error: bad request");
        }


        int gameID = dataAccess.numGames() + 1;
        dataAccess.createGame(new GameData(gameID, null, null, gameName, new ChessGame()));

        return gameID;
    }


    public void joinGame(String authToken, ChessGame.TeamColor playerColor, int gameID) {
        if (!dataAccess.validateAuthToken(authToken)) {
            throw new UnauthorizedException("Error: unauthorized");
        }
        if (!(playerColor == ChessGame.TeamColor.BLACK || playerColor == ChessGame.TeamColor.WHITE)) {
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
            if (game.blackUsername() != null) {
                throw new alreadytakenexception("Error: already taken");
            }

            updatedGame = new GameData(game.gameID(), game.whiteUsername(), userData.username(), game.gameName(), game.game());

        } else if (playerColor == ChessGame.TeamColor.WHITE) {
            if (game.whiteUsername() != null) {
                throw new alreadytakenexception("Error: already taken");
            }
            updatedGame = new GameData(game.gameID(), userData.username(), game.blackUsername(), game.gameName(), game.game());
        } else {
            throw new BadRequestException("Error: bad request");
        }

        dataAccess.updateGame(updatedGame);

    }

}
