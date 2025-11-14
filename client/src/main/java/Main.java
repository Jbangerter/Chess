import chess.*;
import server.Server;

public class Main {
    public static void main(String[] args) {

        try {
            chessServer = new Server();

            (8080).run();

        } catch (Throwable ex) {
            System.out.printf("Unable to start server: %s%n", ex.getMessage());
        }
    }
}