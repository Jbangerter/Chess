package dataaccess;

import model.AuthData;
import model.UserData;

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
    public UserData getUser(String username) {
        return null;
    }

    @Override
    public boolean userExists(String userID) {
        return false;
    }

    @Override
    public void login(AuthData authData) {
        
    }

    @Override
    public boolean userHasAuthdata(String UserID) {
        return false;
    }

    @Override
    public AuthData getAuthdata(String UserID) {
        return null;
    }
}
