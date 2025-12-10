package websocket.commands;

import model.GameData;

public class JoinGameCommand extends UserGameCommand{
    private final String role;

    public JoinGameCommand(CommandType commandType,  String authToken, Integer gameID,String joinsAs) {
        super(commandType, authToken, gameID);
        this.role = joinsAs;
    }

    public String getRole() {
        return role;
    }

}
