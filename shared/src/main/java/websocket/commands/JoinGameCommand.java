package websocket.commands;

public class JoinGameCommand extends UserGameCommand{
    public JoinGameCommand(CommandType commandType, String authToken, Integer gameID) {
        super(commandType, authToken, gameID);
    }
}
