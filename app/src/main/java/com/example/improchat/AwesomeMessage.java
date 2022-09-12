package com.example.improchat;

import android.util.Log;

import androidx.annotation.NonNull;

public class AwesomeMessage {
    private String text;
    private String name;
    private String imageUrl;

    private String time;

    private String sender;
    private String recipient;
    private boolean isMine;

    public AwesomeMessage(){

    }

    public AwesomeMessage(String text, String name, String imageUrl, String time, String sender,
                          String recipient, boolean isMine) {
        this.text = text;
        this.name = name;
        this.imageUrl = imageUrl;
        this.time = time;
        this.sender = sender;
        this.recipient = recipient;
        this.isMine = isMine;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        //Log.d("setName", name);

    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public boolean isMine() {
        return isMine;
    }

    public void setMine(boolean mine) {
        isMine = mine;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
        Log.d("setTime", time);
    }
}
