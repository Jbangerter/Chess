package dataaccess;

import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Objects;

public class MemoryDataAccess implements DataAccess {

    private HashMap<String, UserData> userDB;
    private HashMap<String, AuthData> authDB;
    private HashMap<String, GameData> gameDB;

    public MemoryDataAccess() {
        userDB = new HashMap<>();
        authDB = new HashMap<>();
        gameDB = new HashMap<>();
    }

    @Override
    public void clear() {
        userDB.clear();
        authDB.clear();
        gameDB.clear();
    }

    @Override
    public void createUser(UserData user) {
        userDB.put(user.username(), user);
    }

    @Override
    public boolean userExists(String userID) {
        return userDB.containsKey(userID);
    }

    @Override
    public boolean validatePassword(UserData user) {
        return Objects.equals(user.password(), userDB.get(user.username()).password());
    }

    @Override
    public UserData getUser(String userID) {
        return userDB.get(userID);
    }

    @Override
    public void addAuth(AuthData authData) {
        authDB.put(authData.authToken(), authData);
    }

    @Override
    public boolean validateUserHasAuthdata(AuthData authData) {
        if (authDB.get(authData.authToken()) == null) {
            return false;
        }
        return (Objects.equals(authDB.get(authData.authToken()).username(), authData.username()));
    }

    @Override
    public AuthData getAuthdataFromAuthtoken(String authToken) {
        return authDB.get(authToken);
    }

    @Override
    public boolean validateAuthToken(String authToken) {
        return authTokenExists(authToken);
    }


    @Override
    public boolean authTokenExists(String authToken) {
        return authDB.containsKey(authToken);
    }

    @Override
    public void removeAuth(String authData) {
        authDB.remove(authData);
    }

    @Override
    public void createGame(GameData game) {
        gameDB.put(String.valueOf(game.gameID()), game);
    }

    @Override
    public void updateGame(GameData game) {
        gameDB.remove(String.valueOf(game.gameID()));
        gameDB.put(String.valueOf(game.gameID()), game);
    }

    @Override
    public int numGames() {
        return gameDB.size();
    }

    @Override
    public boolean gameIDExists(int gameID) {
        return gameDB.containsKey(String.valueOf(gameID));
    }

    @Override
    public GameData getGame(int gameID) {
        return gameDB.get(String.valueOf(gameID));
    }

    @Override
    public Collection<GameData> listGames() {
        if (gameDB.values() != null) {
            return gameDB.values();

        } else {
            return Collections.emptyList();
        }
    }

}
