package com;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class ChatPage implements Initializable {
    @FXML
    public ListView<String> userContacts;

    @FXML
    public TextField typingField;

    @FXML
    public TextFlow adreseeTextField;

    @FXML
    public ScrollPane sp;

    @FXML
    public Label adreseeName;

    @FXML
    public TextFlow imagePreview;

    private Map<String, ObservableList<Node>> textFlowsMap = new HashMap<>();
    private String previousUser = "";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            initContactList();
            Thread one = new Thread() {
                public void run() {
                    while (true) {
                        System.out.println("done");
                        try {
                            Object streamObject = ServerConnection.getServerConnectionInstance().input.readObject();
                            if (streamObject != null) {
                                Message m = (Message) streamObject;
                                Platform.runLater(() -> {
                                    //modify your javafx app here.
                                    handleMessage(m);
                                });
                            }
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                            try {
                                Thread.sleep(15000);
                            } catch (InterruptedException e1) {
                                e1.printStackTrace();
                            }
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            one.start();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        typingField.setOnDragOver(getOnDragOverEvent());
        typingField.setOnDragDropped(getOnDragDroppedEvent());
        typingField.setOnKeyPressed(getTypingFieldListenerHandler());
    }


    public void handleListElementClick(MouseEvent event) {
        ObservableList<Node> newTextFlow = textFlowsMap.get(userContacts.getSelectionModel().getSelectedItem());
        textFlowsMap.get(previousUser).addAll(adreseeTextField.getChildren());
        adreseeTextField.getChildren().removeAll(adreseeTextField.getChildren());
        adreseeTextField.getChildren().addAll(newTextFlow);


        previousUser = userContacts.getSelectionModel().getSelectedItem();
        adreseeName.setText(userContacts.getSelectionModel().getSelectedItem());
    }

    public EventHandler<DragEvent> getOnDragOverEvent() {
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
                    if (imagePreview.getChildren().size() > 0) {
                        imagePreview.getChildren().remove(0, 1);
                    }
                    typingField.setText(file.getAbsolutePath());
                    if(file.getAbsolutePath().contains(imageFormats.get(0)) || file.getAbsolutePath().contains(imageFormats.get(1))){
                        Image image = new Image("file:" + file.getAbsolutePath());
                        ImageView imageView = new ImageView();
                        imageView.setFitWidth(image.getWidth() > imagePreview.getWidth() ? imagePreview.getWidth() : image.getWidth());
                        imageView.setPreserveRatio(true);
                        imageView.setImage(image);
                        imagePreview.getChildren().add(imageView);
                    }
                    success = true;
                }
                /*
                 * let the source know whether the string was successfully
                 * transferred and used
                 */
                event.setDropCompleted(success);

                event.consume();
            }

            ;
        };
        return onDragDroppedEvent;
    }

    public EventHandler<KeyEvent> getTypingFieldListenerHandler() {
        EventHandler<KeyEvent> typingFieldListenerHandler = new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent e) {
                if (e.getCode().equals(KeyCode.ENTER)) {
                    try {
                        sendMessage();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        };
        return typingFieldListenerHandler;
    }


    public void initContactList() throws IOException, ClassNotFoundException {
        Message m = new Message(EMessageType.SERVER_USERS, "");
        ServerConnection.getServerConnectionInstance().output.writeObject(m);
        Message returnMessage = (Message) ServerConnection.getServerConnectionInstance().input.readObject();
        handleMessage(returnMessage);
        for (int i = 0; i < userContacts.getItems().size(); i++) {
            textFlowsMap.put(userContacts.getItems().get(i), new TextFlow().getChildren());
        }

        //userContacts = new ListView<>();


    }

    public void sendMessage() throws IOException {
        handleSendingMessage();

    }

    private List<String> imageFormats = Arrays.asList(".JPG", ".PNG");

    public void handleSendingMessage() throws IOException {
        String textOfMessage = typingField.getText();
        String selectedGuy = userContacts.getSelectionModel().getSelectedItem();
        if (imagePreview.getChildren().size() > 0) {
            imagePreview.getChildren().remove(0, 1);
        }
        Message m;
        if (textOfMessage.toUpperCase().contains(imageFormats.get(0))) {
            BufferedImage bufferimage = ImageIO.read(new File(textOfMessage));
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ImageIO.write(bufferimage, "jpg", output);
            byte[] picture = output.toByteArray();
            m = new Message(EMessageType.JPG, picture, selectedGuy, ServerConnection.getServerConnectionInstance().myName);
            ImageView imageView = new ImageView();
            Image image = new Image("file:" + textOfMessage);
            imageView.setImage(image);
            imageView.setFitWidth(image.getWidth() > adreseeTextField.getWidth() ? 500 : image.getWidth());
            imageView.setPreserveRatio(true);
            adreseeTextField.getChildren().add(imageView);
            typingField.clear();
            ServerConnection.getServerConnectionInstance().output.writeObject(m);
        } else if (textOfMessage.toUpperCase().contains(imageFormats.get(1))) {
            BufferedImage bufferimage = ImageIO.read(new File(textOfMessage));
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ImageIO.write(bufferimage, "png", output);
            byte[] picture = output.toByteArray();
            m = new Message(EMessageType.PNG, picture, selectedGuy, ServerConnection.getServerConnectionInstance().myName);
            ImageView imageView = new ImageView();
            Image image = new Image("file:" + textOfMessage);
            imageView.setImage(image);
            imageView.setFitWidth(image.getWidth() > adreseeTextField.getWidth() ? 500 : image.getWidth());
            imageView.setPreserveRatio(true);
            adreseeTextField.getChildren().add(imageView);
            typingField.clear();
            ServerConnection.getServerConnectionInstance().output.writeObject(m);
        } else if(textOfMessage.toUpperCase().contains(".PDF")){
            Path pdfPath = Paths.get(textOfMessage);
            byte[] pdfFile = Files.readAllBytes(pdfPath);
            Label textToAdd = new Label();
            textToAdd.setText(typingField.getText());
            textToAdd.setPrefWidth(570);
            textToAdd.setAlignment(Pos.CENTER_RIGHT);
            adreseeTextField.getChildren().add(textToAdd);
            typingField.clear();
            m = new Message(EMessageType.PDF_FILE, pdfFile, selectedGuy, ServerConnection.getServerConnectionInstance().myName);
            ServerConnection.getServerConnectionInstance().output.writeObject(m);
        }
        else {
            Label textToAdd = new Label();
            textToAdd.setText(typingField.getText());
            textToAdd.setPrefWidth(570);
            textToAdd.setAlignment(Pos.CENTER_RIGHT);
            adreseeTextField.getChildren().add(textToAdd);
            typingField.clear();
            m = new Message(EMessageType.TEXT, textOfMessage, selectedGuy, ServerConnection.getServerConnectionInstance().myName);
            ServerConnection.getServerConnectionInstance().output.writeObject(m);
        }

    }

    public void updateUserContactsList(Message message) {
        String[] userContactsArray = message.getTextMessage().split(",");
        for (String userName : userContactsArray
                ) {
            userContacts.getItems().add(userName);
        }
        userContacts.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        userContacts.getSelectionModel().selectFirst();
        previousUser = userContacts.getSelectionModel().getSelectedItem();
        adreseeName.setText(previousUser);
        //userContacts.getSelectionModel().getSelectedItem();
    }

    public void handleMessage(Message m) {
        switch (m.geteMessageType()) {
            case SERVER_USERS:
                updateUserContactsList(m);
                break;
            case TEXT:
                if (m.getAdressee() != null) {
                    Label textToAdd = new Label();
                    textToAdd.setText(m.getTextMessage());
                    textToAdd.setPrefWidth(570);
                    textToAdd.setAlignment(Pos.CENTER_LEFT);
                    //switchViewWhileHandlingMessage(m);
                    if (userContacts.getSelectionModel().getSelectedItem().equals(m.getSender())) {
                        adreseeTextField.getChildren().add(textToAdd);
                    } else {
                        textFlowsMap.get(m.getSender()).add(textToAdd);
                    }
                    //SocketServer.getUserNameAndPrintWriterMap().get(m.getAdressee()).writeObject(m);
                    sp.vvalueProperty().bind(adreseeTextField.heightProperty());

                }
                break;
            case JPG:
                if (m.getAdressee() != null) {
                    Image i = new Image(new ByteArrayInputStream(m.getFileMessage()));
                    ImageView imageView = new ImageView(i);
                    imageView.setImage(i);
                    imageView.setFitWidth(i.getWidth() > adreseeTextField.getWidth() ? 500 : i.getWidth());
                    imageView.setPreserveRatio(true);
                    //switchViewWhileHandlingMessage(m);
                    if (userContacts.getSelectionModel().getSelectedItem().equals(m.getSender())) {
                        adreseeTextField.getChildren().add(imageView);
                    } else {
                        textFlowsMap.get(m.getSender()).add(imageView);
                    }
                    sp.vvalueProperty().bind(adreseeTextField.heightProperty());
                    /*FileChooser choose = new FileChooser();
                    choose.getExtensionFilters().add(new FileChooser.ExtensionFilter("JPG Image (*.jpg)", "*.jpg"));
                    choose.setInitialFileName("*.jpg");
                    File file = choose.showSaveDialog(typingField.getScene().getWindow());
                    if (file != null) {
                        try {
                            File newFile = new File(file.getPath());
                            OutputStream os = new FileOutputStream(newFile);
                            os.write(m.getFileMessage());
                            os.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }*/
                }
                break;
            case PNG:
                if (m.getAdressee() != null) {
                    {
                        Image i = new Image(new ByteArrayInputStream(m.getFileMessage()));
                        ImageView imageView = new ImageView(i);
                        imageView.setImage(i);
                        imageView.setFitWidth(i.getWidth() > adreseeTextField.getWidth() ? 500 : i.getWidth());
                        imageView.setPreserveRatio(true);
                        if (userContacts.getSelectionModel().getSelectedItem().equals(m.getSender())) {
                            adreseeTextField.getChildren().add(imageView);
                        } else {
                            textFlowsMap.get(m.getSender()).add(imageView);
                        }
                        sp.vvalueProperty().bind(adreseeTextField.heightProperty());
                        FileChooser choose = new FileChooser();
                        choose.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Image (*.png)", "*.png"));
                        choose.setInitialFileName("*.png");
                        File file = choose.showSaveDialog(typingField.getScene().getWindow());
                        if (file != null) {
                            try {
                                File newFile = new File(file.getPath());
                                OutputStream os = new FileOutputStream(newFile);
                                os.write(m.getFileMessage());
                                os.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                break;
            case PDF_FILE:
                if (m.getAdressee() != null) {
                    Label textToAdd = new Label();
                    textToAdd.setText("User was trying to send you a PDF file!");
                    textToAdd.setPrefWidth(570);
                    textToAdd.setAlignment(Pos.CENTER_LEFT);
                    if (userContacts.getSelectionModel().getSelectedItem().equals(m.getSender())) {
                        adreseeTextField.getChildren().add(textToAdd);
                    } else {
                        textFlowsMap.get(m.getSender()).add(textToAdd);
                    }
                    FileChooser choose = new FileChooser();
                    choose.getExtensionFilters().add(new FileChooser.ExtensionFilter("Plik PDF (*.pdf)", "*.pdf"));
                    choose.setInitialFileName("*.pdf");
                    File file = choose.showSaveDialog(typingField.getScene().getWindow());
                    if (file != null) {
                        try {
                            File newFile = new File(file.getPath());
                            OutputStream os = new FileOutputStream(newFile);
                            os.write(m.getFileMessage());
                            os.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
        }
    }

}
