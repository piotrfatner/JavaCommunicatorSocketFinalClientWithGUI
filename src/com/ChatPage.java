package com;

import com.sun.deploy.util.StringUtils;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;

public class ChatPage implements Initializable{
    @FXML
    public ListView<String> userContacts;

    @FXML
    public TextField typingField;

    @FXML
    public TextFlow adreseeTextField;

    @FXML
    public ScrollPane sp;

    private Map<String,TextFlow> textFlowsMap = new HashMap<>();
    private String previousUser = "Piotr1";

    public void handleListElementClick(MouseEvent event){
        System.out.println("clicked on " + userContacts.getSelectionModel().getSelectedItem());
        /*TextFlow newTextFlow =  textFlowsMap.get(userContacts.getSelectionModel().getSelectedItem());
        textFlowsMap.put(previousUser, adreseeTextField);
        adreseeTextField = newTextFlow;
        previousUser = userContacts.getSelectionModel().getSelectedItem();*/
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
        typingField.setOnDragOver(getOnDragOverEvent());
        typingField.setOnDragDropped(getOnDragDroppedEvent());
    }

    public EventHandler<DragEvent> getOnDragOverEvent(){
        EventHandler<DragEvent> newOnDragOverEvent = new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                if (event.getGestureSource() != typingField && event.getDragboard().hasFiles()) {
                    /*
                     * allow for both copying and moving, whatever user chooses
                     */
                    event.acceptTransferModes(TransferMode.COPY);
                }
                event.consume();

            }
        };
        return newOnDragOverEvent;
    }

    public EventHandler<DragEvent> getOnDragDroppedEvent() {
        EventHandler<DragEvent> onDragDroppedEvent = new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                Dragboard db = event.getDragboard();
                List<File> files = (ArrayList<File>) db.getContent(DataFormat.FILES);

                boolean success = false;
                if (files != null) {
                    File file = files.get(0);
                    typingField.setText(file.getAbsolutePath());
                    success = true;
                }
                /*
                 * let the source know whether the string was successfully
                 * transferred and used
                 */
                event.setDropCompleted(success);

                event.consume();
            };
        };
        return onDragDroppedEvent;
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
        handleSendingMessage();

    }
    private List<String> imageFormats = Arrays.asList(".JPG",".PNG");

    public void handleSendingMessage() throws IOException {
        String textOfMessage = typingField.getText();
        String selectedGuy = userContacts.getSelectionModel().getSelectedItem();
        Message m;
        if(textOfMessage.toUpperCase().contains(imageFormats.get(0))){
            BufferedImage bufferimage = ImageIO.read(new File(textOfMessage));
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ImageIO.write(bufferimage, "jpg", output );
            byte [] picture = output.toByteArray();
            m = new Message(EMessageType.JPG, picture, selectedGuy);
            ImageView imageView = new ImageView();
            Image image = new Image("file:"+textOfMessage);
            imageView.setImage(image);
            imageView.setFitHeight(600);
            imageView.setFitWidth(600);
            adreseeTextField.getChildren().add(imageView);
            typingField.clear();
            ServerConnection.getServerConnectionInstance().output.writeObject(m);
        } else{
            Text textToAdd = new Text(typingField.getText()+"\n");
            adreseeTextField.getChildren().add(textToAdd);
            typingField.clear();
            m = new Message(EMessageType.TEXT, textOfMessage, selectedGuy);
            ServerConnection.getServerConnectionInstance().output.writeObject(m);
        }

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
                break;
            case TEXT:
                if(m.getAdressee() != null){
                    Text newMessage = new Text(m.getTextMessage()+"\n");
                    adreseeTextField.getChildren().add(newMessage);
                    //SocketServer.getUserNameAndPrintWriterMap().get(m.getAdressee()).writeObject(m);
                    sp.vvalueProperty().bind(adreseeTextField.heightProperty());

                }
                break;
            case JPG:
                if(m.getAdressee()!= null){
                    Image i = new Image(new ByteArrayInputStream(m.getFileMessage()));
                    ImageView imageView = new ImageView(i);
                    imageView.setFitHeight(600);
                    imageView.setFitWidth(600);
                    adreseeTextField.getChildren().add(imageView);
                    FileChooser aa = new FileChooser();
                    sp.vvalueProperty().bind(adreseeTextField.heightProperty());
                    File dest = aa.showSaveDialog(typingField.getScene().getWindow());
                    if (dest != null) {
                        try {
                            File newFile = new File("C:/Users/fatne/IdeaProjects/JavaCommunicatorSocketProject/JavaCommunicatorSocketFinalClientWithGUI/src/com/newPicture.jpg");
                            OutputStream os = new FileOutputStream(newFile);
                            os.write(m.getFileMessage());
                            os.close();
                            Files.copy(newFile.toPath(), dest.toPath());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                break;

        }
    }
}
