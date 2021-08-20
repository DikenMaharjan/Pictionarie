package com.example.pictionarie.model;

public class Player {
    String name;
    public boolean host;
    String socketId;
    public boolean ready;

    public Player(){
    }


    public Player(String name, boolean host) {
        this.name = name;
        this.host = host;
        ready = false;
    }

    public String getName() {
        return name;
    }

    public String getSocketId() {
        return socketId;
    }

    public void setSocketId(String socketId) {
        this.socketId = socketId;
    }

    public void setName(String name) {
        this.name = name;
    }
}
