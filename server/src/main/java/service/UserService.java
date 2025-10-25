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
        dataAccess.addAuth(authData);

        return authData;
    }


    public AuthData login(UserData user) {
        if (user.username() == null || user.password() == null) {
            throw new BadRequestException("Error: bad request");
        }
        //make sure the User exits
        if (!dataAccess.userExists(user.username())) {
            throw new UnauthorizedException("Error: unauthorized");
        }
        if (!dataAccess.validPasword(user)) {
            throw new UnauthorizedException("Error: unauthorized");
        }

        AuthData authData = new AuthData(user.username(), generateAuthToken());
        dataAccess.addAuth(authData);
        return authData;
    }


    private String generateAuthToken() {
        return UUID.randomUUID().toString();
    }

}

