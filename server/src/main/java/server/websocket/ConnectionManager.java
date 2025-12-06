package server.websocket;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import service.GameService;
import service.UserService;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {
    public final ConcurrentHashMap<Integer, ArrayList<Session>> connections = new ConcurrentHashMap<>();

    public void add(int gameID, Session session) {
        connections.computeIfAbsent(gameID, k -> new ArrayList<>());
        connections.get(gameID).add(session);
    }

    public void remove(int gameID, Session session) {
        if (connections.containsKey(gameID)) {
            connections.get(gameID).remove(session);

            if (connections.get(gameID).isEmpty()) {
                connections.remove(gameID);
            }
        }
    }

    public void broadcast(int gameID, ServerMessage notification, Session excludeSession) throws IOException {
        var gameSessions = connections.get(gameID);

        if (gameSessions != null) {
            var removeList = new ArrayList<Session>();

            for (var session : gameSessions) {
                if (session.isOpen()) {
                    // dont send the message to  user who performed the action
                    if (!session.equals(excludeSession)) {
                        String json = new Gson().toJson(notification);
                        session.getRemote().sendString(json);
                    }
                } else {
                    removeList.add(session);
                }
            }

            for (var session : removeList) {
                gameSessions.remove(session);
            }
        }
    }
}