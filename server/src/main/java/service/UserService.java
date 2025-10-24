package service;


import dataaccess.MemoryDataAccess;
import model.*;

import java.util.UUID;


public class UserService {
    public UserService(MemoryDataAccess dataAccess) {
    }

    public AuthData register(UserData user) {
        return new AuthData(user.username(), generateAuthToken());
    }

    private String generateAuthToken() {
        return UUID.randomUUID().toString();
    }

}

