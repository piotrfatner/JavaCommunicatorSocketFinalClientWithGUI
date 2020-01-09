package com;


import java.io.Serializable;

public class Message implements Serializable{
    private EMessageType eMessageType;
    private String textMessage;
    private byte[] fileMessage;
    private String adressee;

    public Message(EMessageType messageType, String text){
        this.eMessageType = messageType;
        this.textMessage = text;
    }

    public Message(EMessageType messageType, byte[] fileMessage){
        this.eMessageType = messageType;
        this.fileMessage = fileMessage;
    }

    public Message(EMessageType messageType, String text, String adressee){
        this.eMessageType = messageType;
        this.textMessage = text;
        this.adressee = adressee;
    }

    public EMessageType geteMessageType() {
        return eMessageType;
    }

    public String getTextMessage() {
        return textMessage;
    }

    public byte[] getFileMessage() {
        return fileMessage;
    }

    public String getAdressee() {
        return adressee;
    }
}
