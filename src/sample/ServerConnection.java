package sample;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class ServerConnection {
    private static ServerConnection serverConnectionInstance = null;
    public String myName;
    public Socket socket;
    public Scanner input;
    public PrintWriter output;

    public static ServerConnection getServerConnectionInstance(){
        if(serverConnectionInstance == null){
            serverConnectionInstance= new ServerConnection();
        }
        return serverConnectionInstance;
    }

    public void runServerConnection() throws IOException {
        Socket socket = new Socket("127.0.0.1", 59001);
        input = new Scanner(socket.getInputStream());
        output = new PrintWriter(socket.getOutputStream(), true);
    }
}
