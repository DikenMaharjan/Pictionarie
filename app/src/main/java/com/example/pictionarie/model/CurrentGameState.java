package com.example.pictionarie.model;

public class CurrentGameState {
    int round;
    int turn;
    boolean ready;
    String drawerName;
    String drawerId;
    String chosenWord;

    public int getRound() {
        return round;
    }

    public void setRound(int round) {
        this.round = round;
    }

    public int getTurn() {
        return turn;
    }

    public void setTurn(int turn) {
        this.turn = turn;
    }

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    public String getDrawerName() {
        return drawerName;
    }

    public void setDrawerName(String drawerName) {
        this.drawerName = drawerName;
    }

    public String getDrawerId() {
        return drawerId;
    }

    public void setDrawerId(String drawerId) {
        this.drawerId = drawerId;
    }

    public String getChosenWord() {
        return chosenWord;
    }

    public void setChosenWord(String chosenWord) {
        this.chosenWord = chosenWord;
    }

    public CurrentGameState(int round, int turn, boolean ready, String drawerName, String drawerId, String chosenWord) {
        this.round = round;
        this.turn = turn;
        this.ready = ready;
        this.drawerName = drawerName;
        this.drawerId = drawerId;
        this.chosenWord = chosenWord;
    }

    public CurrentGameState() {
    }
}
