package dataaccess;

import model.AuthData;
import model.GameData;
import model.UserData;

public interface DataAccess {


    void clear();

    void createUser(UserData user);

    UserData getUser(String userID);

    boolean userExists(String userID);

    boolean validatePassword(UserData user);

    void addAuth(AuthData authData);

    boolean validateUserHasAuthdata(AuthData authData);

    AuthData getAuthdataFromAuthtoken(String authToken);

    boolean validateAuthToken(String authToken);

    boolean authTokenExists(String authToken);

    void removeAuth(String authData);


    void createGame(GameData game);

    void updateGame(GameData game);

    int numGames();

    boolean gameIDExists(int gameID);

    GameData getGame(int gameID);

    GameData[] listGames();


}
