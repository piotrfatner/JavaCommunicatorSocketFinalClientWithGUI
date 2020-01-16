package com;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class LoginController {
    public ObjectInputStream in;
    public ObjectOutputStream out;
    @FXML
    public TextField userNameField;

    public void loginToServer(ActionEvent event) throws IOException, ClassNotFoundException {
        out = ServerConnection.getServerConnectionInstance().output;
        in = ServerConnection.getServerConnectionInstance().input;

        while (true) {
            Message m = (Message) ServerConnection.getServerConnectionInstance().input.readObject();
            if (m.geteMessageType().equals(EMessageType.SERVER_WAITING)) {
                Message messageBack = new Message(EMessageType.TEXT,userNameField.getText());
                out.writeObject(messageBack);
            } else if (m.geteMessageType().equals(EMessageType.SERVER_ACCEPTED)) {
                ServerConnection.getServerConnectionInstance().myName = userNameField.getText();
                changeScreen(event);
                break;
            } else if(m.geteMessageType().equals(EMessageType.SERVER_REJECTED)){
                userNameField.clear();
                break;
            }
        }
    }

    public void changeScreen(ActionEvent event) throws IOException {
        Parent homePageParent = FXMLLoader.load(getClass().getResource("ChatPage.fxml"));
        Scene homePageScene = new Scene(homePageParent);
        Stage appStage = (Stage) ((Node)event.getSource()).getScene().getWindow();
        appStage.setScene(homePageScene);
    }
}
