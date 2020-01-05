package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class LoginController {
    public Scanner in;
    public PrintWriter out;
    @FXML
    public TextField userNameField;

    public void loginToServer(ActionEvent event) throws IOException {
        in = ServerConnection.getServerConnectionInstance().input;
        out = ServerConnection.getServerConnectionInstance().output;

        while (in.hasNextLine()) {
            String line = in.nextLine();
            if (line.startsWith("Write name:")) {
                out.println(userNameField.getText());
            } else if (line.startsWith("USER NAME ACCEPTED")) {
                //ServerConnection.getServerConnectionInstance().myName = userNameField.getText();
                changeScreen(event);
                break;
            }
        }
    }

    public void changeScreen(ActionEvent event) throws IOException {
        Parent homePageParent = FXMLLoader.load(getClass().getResource("ChatPage.fxml"));
        Scene homePageScene = new Scene(homePageParent);
        Stage appStage = (Stage) ((Node)event.getSource()).getScene().getWindow();
        appStage.setScene(homePageScene);
        appStage.show();
    }
}
