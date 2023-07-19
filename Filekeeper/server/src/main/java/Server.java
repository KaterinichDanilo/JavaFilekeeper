import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private ServerSocket server;
    private Socket socket;
    private final int PORT = 5000;

    public Server() {
        try {
            server = new ServerSocket(PORT);
            System.out.println("Server started!");
            while (!server.isClosed()) {
                socket = server.accept();
                System.out.println("New client connected!");
                new ClientHandler(this, this.socket);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                socket.close();
                server.close();
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }

    }

}
