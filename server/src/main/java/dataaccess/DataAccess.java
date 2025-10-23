package dataaccess;

import model.UserData;

public interface DataAccess {


    void clear();

    void createUser(UserData user);

    UserData getUser(String username);

}
