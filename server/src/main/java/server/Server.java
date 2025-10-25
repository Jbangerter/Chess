package server;

import Exceptions.*;
import com.google.gson.Gson;

import dataaccess.MemoryDataAccess;
import io.javalin.*;

import java.util.*;

import io.javalin.json.JavalinGson;
import io.javalin.http.Context;
import org.jetbrains.annotations.NotNull;
import service.*;
import model.*;

public class Server {

    private final Javalin javalin;

    private MemoryDataAccess dataAccess;
    private UserService userService;

    public Server() {
        dataAccess = new MemoryDataAccess();
        userService = new UserService(dataAccess);

        //javalin = Javalin.create(config -> config.staticFiles.add("web"));


        javalin = Javalin.create(config -> {
            config.staticFiles.add("web");
            config.jsonMapper(new JavalinGson()); // Use Javalin's Gson plugin
        });


        javalin.exception(BadRequestException.class, (e, ctx) -> {
            ctx.status(400).json(new ErrorResponse(e.getMessage())); // Bad Request status
        });

        javalin.exception(UnauthorizedException.class, (e, ctx) -> {
            ctx.status(401).json(new ErrorResponse(e.getMessage())); // Forbidden status
        });

        javalin.exception(AlreadyTakenException.class, (e, ctx) -> {
            ctx.status(403).json(new ErrorResponse(e.getMessage())); // Forbidden status
        });


        //JAVALIN EVENTS
        javalin.delete("/db", this::deleteAll);
        javalin.post("/user", this::register);
        javalin.post("/session", this::login);
    }

    private void deleteAll(@NotNull Context ctx) {
        dataAccess.clear();
    }

    private void register(@NotNull Context ctx) {

        var serializer = new Gson();
        String reqJson = ctx.body();
        var req = serializer.fromJson(reqJson, UserData.class);

        var response = this.userService.register(req);
        ctx.status(200).json(response);
    }

    private void login(@NotNull Context ctx) {

        var serializer = new Gson();
        String reqJson = ctx.body();
        var req = serializer.fromJson(reqJson, UserData.class);

        var response = this.userService.login(req);
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
