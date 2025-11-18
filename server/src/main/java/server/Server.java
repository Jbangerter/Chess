package server;

import dataaccess.DataAccessException;
import dataaccess.SqlDataAccess;
import exceptions.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import io.javalin.*;

import io.javalin.json.JavalinGson;
import io.javalin.http.Context;
import org.jetbrains.annotations.NotNull;
import service.*;
import model.*;
import model.gameservicerecords.CreateGameInput;
import model.gameservicerecords.JoinGameInput;

public class Server {

    private final Javalin javalin;

    private SqlDataAccess dataAccess;
    private UserService userService;
    private GameService gameService;

    public Server() {
        dataAccess = new SqlDataAccess();
        userService = new UserService(dataAccess);
        gameService = new GameService(dataAccess);

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
        javalin.exception(DataAccessException.class, (e, ctx) -> {
            System.err.println("Database Error occurred: " + e.getMessage());
            ctx.status(500).json(new ErrorResponse("Internal server error: " + e.getMessage()));
        });


        //JAVALIN EVENTS
        javalin.delete("/db", this::deleteAll);
        javalin.post("/user", this::register);
        javalin.post("/session", this::login);
        javalin.delete("/session", this::logout);
        javalin.post("/game", this::createGame);
        javalin.put("/game", this::joinGame);
        javalin.get("/game", this::listGames);
    }

    private void deleteAll(@NotNull Context ctx) throws DataAccessException {
        dataAccess.clear();
    }

    private void register(@NotNull Context ctx) throws DataAccessException {

        var serializer = new Gson();
        String reqJson = ctx.body();
        var req = serializer.fromJson(reqJson, UserData.class);

        var response = this.userService.register(req);
        ctx.status(200).json(response);
    }

    private void login(@NotNull Context ctx) throws DataAccessException {

        var serializer = new Gson();
        String reqJson = ctx.body();
        var req = serializer.fromJson(reqJson, UserData.class);

        var response = this.userService.login(req);
        ctx.status(200).json(response);
    }

    private void logout(@NotNull Context ctx) throws DataAccessException {

        var req = ctx.header("Authorization");

        this.userService.logout(req);
        ctx.status(200).json("{}");
    }

    private void createGame(@NotNull Context ctx) throws DataAccessException {

        var authToken = ctx.header("Authorization");

        var serializer = new Gson();
        String reqJson = ctx.body();
        var req = serializer.fromJson(reqJson, CreateGameInput.class);

        var response = this.gameService.createGame(authToken, req.gameName());

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("gameID", response);


        ctx.status(200).json(jsonObject);

    }

    private void joinGame(@NotNull Context ctx) throws DataAccessException {

        var authToken = ctx.header("Authorization");

        var serializer = new Gson();
        String reqJson = ctx.body();
        var req = serializer.fromJson(reqJson, JoinGameInput.class);

        GameData game = this.gameService.joinGame(authToken, req.playerColor(), req.gameID());

        ctx.status(200).json(game);
    }

    private void listGames(@NotNull Context ctx) throws DataAccessException {

        var authToken = ctx.header("Authorization");

        var gamesList = this.gameService.listGames(authToken);

        if (gamesList == null) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("game", "[]");

            ctx.status(200).json(jsonObject);
        }

        ctx.status(200).json(gamesList);

    }


    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
