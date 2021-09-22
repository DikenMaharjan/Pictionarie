package com.example.pictionarie.model;


public class Messages {
    String message;
    Player player;

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Messages() {
    }

    public Messages(String message, Player player) {
        this.message = message;
        this.player = player;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
