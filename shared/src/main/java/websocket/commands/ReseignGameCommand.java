package websocket.commands;

public class ReseignGameCommand extends UserGameCommand{
    public ReseignGameCommand(CommandType commandType, String authToken, Integer gameID) {
        super(commandType, authToken, gameID);
    }
}
