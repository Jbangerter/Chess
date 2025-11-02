package dataaccess;

import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.Collection;

public interface DataAccess {


    void clear() throws DataAccessException;

    void createUser(UserData user) throws DataAccessException;

    UserData getUser(String userID) throws DataAccessException;

    boolean userExists(String userID) throws DataAccessException;

    boolean validatePassword(UserData user) throws DataAccessException;

    void addAuth(AuthData authData) throws DataAccessException;

    boolean validateUserHasAuthdata(AuthData authData) throws DataAccessException;

    AuthData getAuthdataFromAuthtoken(String authToken) throws DataAccessException;

    boolean validateAuthToken(String authToken) throws DataAccessException;

    boolean authTokenExists(String authToken) throws DataAccessException;

    void removeAuth(String authData) throws DataAccessException;


    void createGame(GameData game) throws DataAccessException;

    void updateGame(GameData game) throws DataAccessException;

    int numGames() throws DataAccessException;

    boolean gameIDExists(int gameID) throws DataAccessException;

    GameData getGame(int gameID) throws DataAccessException;

    Collection<GameData> listGames() throws DataAccessException;


}
