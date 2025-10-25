package dataaccess;

import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.HashMap;

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
    public UserData getUser(String userID) {
        return userDB.get(userID);
    }

    @Override
    public void login(AuthData authData) {
        authDB.put(authData.username(), authData);
    }

    @Override
    public boolean userHasAuthdata(String UserID) {
        return authDB.containsKey(UserID);
    }

    @Override
    public AuthData getAuthdata(String UserID) {
        return authDB.get(UserID);
    }


}
