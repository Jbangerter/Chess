package server;

import com.google.gson.Gson;

import dataaccess.MemoryDataAccess;
import io.javalin.*;

import java.util.*;

import io.javalin.http.Context;
import org.jetbrains.annotations.NotNull;
import service.*;
import model.*;

public class Server {

    private final Javalin javalin;
    private UserService userService;

    public Server() {
        var dataAccess = new MemoryDataAccess();
        UserService userService = new UserService(dataAccess);

        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        // Register your endpoints and exception handlers here.
        javalin.post("/user", Server::register);
        javalin.delete("/db", Server::deleteAll);
    }

    private static void deleteAll(@NotNull Context ctx) {

    }

    private static void register(@NotNull Context ctx) {

        var serializer = new Gson();
        String reqJson = ctx.body();
        var req = serializer.fromJson(reqJson, UserData.class);

        var response = Server.userService.register(req);
        ctx.status(200).json(response);
    }


    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
