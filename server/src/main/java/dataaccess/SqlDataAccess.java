package dataaccess;

import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.Collection;
import java.util.List;

public class SqlDataAccess implements DataAccess {

    public SqlDataAccess() throws DataAccessException {

    }

    @Override
    public void clear() {

    }

    @Override
    public void createUser(UserData user) {

    }

    @Override
    public UserData getUser(String userID) {
        return null;
    }

    @Override
    public boolean userExists(String userID) {
        return false;
    }

    @Override
    public boolean validatePassword(UserData user) {
        return false;
    }

    @Override
    public void addAuth(AuthData authData) {

    }

    @Override
    public boolean validateUserHasAuthdata(AuthData authData) {
        return false;
    }

    @Override
    public AuthData getAuthdataFromAuthtoken(String authToken) {
        return null;
    }

    @Override
    public boolean validateAuthToken(String authToken) {
        return false;
    }

    @Override
    public boolean authTokenExists(String authToken) {
        return false;
    }

    @Override
    public void removeAuth(String authData) {

    }

    @Override
    public void createGame(GameData game) {

    }

    @Override
    public void updateGame(GameData game) {

    }

    @Override
    public int numGames() {
        return 0;
    }

    @Override
    public boolean gameIDExists(int gameID) {
        return false;
    }

    @Override
    public GameData getGame(int gameID) {
        return null;
    }

    @Override
    public Collection<GameData> listGames() {
        return List.of();
    }
}