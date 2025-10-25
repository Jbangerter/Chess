package service;


import Exceptions.*;
import dataaccess.MemoryDataAccess;
import model.*;

import java.util.UUID;


public class UserService {

    private final MemoryDataAccess dataAccess;

    public UserService(MemoryDataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public AuthData register(UserData user) {
        if (user.username() == null || user.email() == null || user.password() == null) {
            throw new BadRequestException("Error: bad request");
        }
        if (dataAccess.userExists(user.username())) {
            throw new AlreadyTakenException("Error: already taken");
        }

        AuthData authData = new AuthData(user.username(), generateAuthToken());

        dataAccess.createUser(user);
        dataAccess.login(authData);

        return authData;
    }

    private String generateAuthToken() {
        return UUID.randomUUID().toString();
    }

}

