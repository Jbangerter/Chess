package service;

import dataaccess.DataAccessException;
import dataaccess.SqlDataAccess;
import exceptions.*;
import chess.ChessGame;
import model.*;
import model.gameservicerecords.GameListData;
import model.gameservicerecords.ShortenedGameData;

import java.util.ArrayList;
import java.util.Collection;

public class GameService {

    private final SqlDataAccess dataAccess;

    public GameService(SqlDataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }


    public GameListData listGames(String authToken) throws DataAccessException {
        if (!dataAccess.validateAuthToken(authToken)) {
            throw new UnauthorizedException("Error: unauthorized");
        }

        Collection<ShortenedGameData> gameList = new ArrayList<>();

        Collection<GameData> unprocessedGames = dataAccess.listGames();

        for (GameData game : unprocessedGames) {
            gameList.add(new ShortenedGameData(game.gameID(), game.whiteUsername(), game.blackUsername(), game.gameName()));
        }

        return new GameListData(gameList);
    }

    public int createGame(String authToken, String gameName) throws DataAccessException {
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


    public GameData joinGame(String authToken, ChessGame.TeamColor playerColor, int gameID) throws DataAccessException {
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
            if ((game.blackUsername() != null) && (!game.blackUsername().equals(userAuthdata.username()))) {
                throw new AlreadyTakenException("Error: already taken");
            }

            updatedGame = new GameData(game.gameID(), game.whiteUsername(), userData.username(), game.gameName(), game.game());

        } else if (playerColor == ChessGame.TeamColor.WHITE) {
            if ((game.whiteUsername() != null) && (!game.whiteUsername().equals(userAuthdata.username()))) {
                throw new AlreadyTakenException("Error: already taken");
            }
            updatedGame = new GameData(game.gameID(), userData.username(), game.blackUsername(), game.gameName(), game.game());
        } else {
            throw new BadRequestException("Error: bad request");
        }

        dataAccess.updateGame(updatedGame);
        return dataAccess.getGame(updatedGame.gameID());

    }


    public GameData joinGameObserver(String authToken, int gameID) throws DataAccessException {
        if (!dataAccess.validateAuthToken(authToken)) {
            throw new UnauthorizedException("Error: unauthorized");
        }
        if (!dataAccess.gameIDExists(gameID)) {
            throw new BadRequestException("Error: bad request");
        }

        GameData game = dataAccess.getGame(gameID);

        dataAccess.updateGame(game);
        return dataAccess.getGame(game.gameID());

    }

}
