package com;

import javafx.application.Platform;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.io.IOException;

public class ServerPing extends ScheduledService<Void> {
    private TextFlow textFlow;
    public ServerPing(TextFlow textFlow){
        this.textFlow = textFlow;
    }
    @Override
    protected Task<Void> createTask(){
        return new Task<Void>(){
            @Override
            protected Void call(){
                Platform.runLater(() -> {
              /* Modify you GUI properties... */
                    Message m = null;
                    try {
                        m = (Message) ServerConnection.getServerConnectionInstance().input.readObject();
                        handleMessage(m);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                });
                return null;
            }
        };
    }
    public void handleMessage(Message m){
        switch (m.geteMessageType()){
            case TEXT:
                if(m.getAdressee() != null){
                    Text newMessage = new Text(m.getTextMessage());
                    textFlow.getChildren().add(newMessage);
                    //SocketServer.getUserNameAndPrintWriterMap().get(m.getAdressee()).writeObject(m);
                }

        }
    }
}

