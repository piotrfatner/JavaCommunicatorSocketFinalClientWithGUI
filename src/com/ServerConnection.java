package com;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ServerConnection {
    private static ServerConnection serverConnectionInstance = null;
    public String myName;
    public Socket socket;
    public ObjectInputStream input;
    public ObjectOutputStream output;

    public static ServerConnection getServerConnectionInstance(){
        if(serverConnectionInstance == null){
            serverConnectionInstance= new ServerConnection();
        }
        return serverConnectionInstance;
    }

    public void runServerConnection() throws IOException {
        Socket socket = new Socket("127.0.0.1", 59001);
        output = new ObjectOutputStream(socket.getOutputStream());
        input = new ObjectInputStream(socket.getInputStream());
    }
}
