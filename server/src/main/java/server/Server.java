package server;

import com.google.gson.Gson;
import datamodel.*;
import io.javalin.*;
import service.UserService;

public class Server {

    private final Javalin javalin;

    private final UserService userService;

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        // Register your endpoints and exception handlers here.

    }


    private void register(context ctx){
        var serializer = new Gson();
        String reqJson = ctx.body();
        var req = serializer.from(reqJson, UserData.class)

        userService

        var authData = userService.register(user);

    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
