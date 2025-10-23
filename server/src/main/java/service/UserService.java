package service;

import model.*;

public class UserService {
    public AuthData register(UserData user) {
        return new AuthData(user.username(), generateAuthToken());
    }

    private String generateAuthToken() {
        return "xyz";
    }

}

