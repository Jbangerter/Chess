package dataaccess;

import model.AuthData;
import model.UserData;

public interface DataAccess {


    void clear();

    void createUser(UserData user);

    UserData getUser(String userID);

    boolean userExists(String userID);

    boolean validPasword(UserData user);

    void addAuth(AuthData authData);

    boolean userHasAuthdata(String UserID);

    AuthData getAuthdata(String UserID);

}
