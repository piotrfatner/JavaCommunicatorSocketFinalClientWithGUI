package com;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class ChatPage implements Initializable{
    @FXML
    public ListView<String> userContacts;

    @FXML
    public TextField typingField;

    @FXML
    public TextFlow adreseeTextField;

    private Map<String,TextFlow> textFlowsMap = new HashMap<>();
    private String previousUser = "Piotr1";

    public void handleListElementClick(MouseEvent event){
        System.out.println("clicked on " + userContacts.getSelectionModel().getSelectedItem());
        TextFlow newTextFlow =  textFlowsMap.get(userContacts.getSelectionModel().getSelectedItem());
        textFlowsMap.put(previousUser, adreseeTextField);
        adreseeTextField = newTextFlow;
        previousUser = userContacts.getSelectionModel().getSelectedItem();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            initContactList();
            Thread one = new Thread() {
                public void run() {
                    while (true){
                        System.out.println("done");
                        try {
                            Object streamObject = ServerConnection.getServerConnectionInstance().input.readObject();
                            if(streamObject!=null){
                                Message m = (Message) streamObject;
                                Platform.runLater(()->{
                                    //modify your javafx app here.
                                    handleMessage(m);
                                });
                            }
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            one.start();
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
        for (int i=0; i<userContacts.getItems().size();i++){
            textFlowsMap.put(userContacts.getItems().get(i), new TextFlow());
        }

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
                    Text newMessage = new Text(m.getTextMessage()+"\n");
                    adreseeTextField.getChildren().add(newMessage);
                    //SocketServer.getUserNameAndPrintWriterMap().get(m.getAdressee()).writeObject(m);
                }

        }
    }
}
