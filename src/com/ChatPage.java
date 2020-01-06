package com;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.ResourceBundle;

public class ChatPage implements Initializable{
    @FXML
    public ListView<String> userContacts;

    @FXML
    public TextField typingField;

    @FXML
    public TextFlow adreseeTextField;

    private Map<String,TextFlow> textFlowsMap;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            initContactList();
            new Thread() {

                // runnable for that thread
                public void run() {
                    for (int i = 0; i < 50; i++) {
                        try {
                            // imitating work
                            Thread.sleep(new Random().nextInt(1000));
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();

                        }
                        final double progress = i*0.05;
                        // update ProgressIndicator on FX thread
                        Platform.runLater(new Runnable() {

                            public void run() {
                                System.out.println(progress);
                            }
                        });
                    }
                }
            }.start();
            //keepListeningServer();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void initContactList() throws IOException, ClassNotFoundException {
        //ServerConnection.getServerConnectionInstance().output.println("GET_CONTANCT_LIST");
        //String userNames = ServerConnection.getServerConnectionInstance().input.nextLine();

        Message m = new Message(EMessageType.SERVER_USERS, "");
        ServerConnection.getServerConnectionInstance().output.writeObject(m);
        Message returnMessage = (Message) ServerConnection.getServerConnectionInstance().input.readObject();
        handleMessage(returnMessage);

        //userContacts = new ListView<>();


    }

    /*public void remindToListen(){
        timeline = new Timeline(new KeyFrame(
                Duration.millis(1000),
                ae -> keepListeningServer()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }*/

    public void keepListeningServer() throws IOException, ClassNotFoundException {
        Message m = (Message) ServerConnection.getServerConnectionInstance().input.readObject();
        handleMessage(m);
    }

    public void sendMessage() throws IOException {
        String textOfMessage = typingField.getText();
        String selectedGuy = userContacts.getSelectionModel().getSelectedItem();
        Message m = new Message(EMessageType.TEXT, textOfMessage, selectedGuy);
        Text textToAdd = new Text(typingField.getText());
        adreseeTextField.getChildren().add(textToAdd);
        typingField.clear();
        ServerConnection.getServerConnectionInstance().output.writeObject(m);

    }

    public void updateUserContactsList(Message message){
        String[] userContactsArray = message.getTextMessage().split(",");
        for (String userName:userContactsArray
                ) {
            userContacts.getItems().add(userName);
        }
        userContacts.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        userContacts.getSelectionModel().getSelectedItem();
    }

    public void handleMessage(Message m){
        switch (m.geteMessageType()){
            case SERVER_USERS:
                updateUserContactsList(m);
            case TEXT:
                if(m.getAdressee() != null){
                    Text newMessage = new Text(m.getTextMessage());
                    adreseeTextField.getChildren().add(newMessage);
                    //SocketServer.getUserNameAndPrintWriterMap().get(m.getAdressee()).writeObject(m);
                }

        }
    }
}
