package service;


import dataaccess.DataAccessException;
import dataaccess.SqlDataAccess;
import exceptions.*;
import dataaccess.MemoryDataAccess;
import model.*;

import java.util.UUID;


public class UserService {

    private final SqlDataAccess dataAccess;

    public UserService(SqlDataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public AuthData register(UserData user) throws DataAccessException {
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


    public AuthData login(UserData user) throws DataAccessException {
        if (user.username() == null || user.password() == null) {
            throw new BadRequestException("Error: bad request");
        }
        //make sure the User exits
        if (!dataAccess.userExists(user.username())) {
            throw new UnauthorizedException("Error: unauthorized");
        }
        if (!dataAccess.validatePassword(user)) {
            throw new UnauthorizedException("Error: unauthorized");
        }

        AuthData authData = new AuthData(user.username(), generateAuthToken());
        dataAccess.addAuth(authData);
        return authData;
    }


    public void logout(String authData) throws DataAccessException {
        //make sure the User exits
        if (!dataAccess.authTokenExists(authData)) {
            throw new UnauthorizedException("Error: unauthorized");
        }
        dataAccess.removeAuth(authData);
    }

    private String generateAuthToken() {
        return UUID.randomUUID().toString();
    }

}

