package sample;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class ChatPage implements Initializable{
    @FXML
    public ListView<String> userContacts;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println(ServerConnection.getServerConnectionInstance().input.nextLine());
        initContactList();
    }

    public void initContactList(){
        ServerConnection.getServerConnectionInstance().output.println("GET_CONTANCT_LIST");
        String userNames = ServerConnection.getServerConnectionInstance().input.nextLine();
        userContacts = new ListView<>();
        String[] userContactsArray = userNames.split(",");
        for (String userName:userContactsArray
             ) {
            userContacts.getItems().add(userName);
        }
        userContacts.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

    }
}
