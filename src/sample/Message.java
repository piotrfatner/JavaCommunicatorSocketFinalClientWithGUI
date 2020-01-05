package sample;


public class Message {
    private EMessageType eMessageType;
    private String textMessage;
    private byte[] fileMessage;

    public Message(EMessageType messageType, String text){
        this.eMessageType = messageType;
        this.textMessage = text;
    }

    public Message(EMessageType messageType, byte[] fileMessage){
        this.eMessageType = messageType;
        this.fileMessage = fileMessage;
    }
}
