package com.example.pictionarie.model;

public class GameInformation {

    String chosenWord;

    public String getChosenWord() {
        return chosenWord;
    }

    public void setChosenWord(String chosenWord) {
        this.chosenWord = chosenWord;
    }

    int rounds;
    int time;
    boolean started;
    public GameInformation() {
    }
    public GameInformation(int rounds, int time, boolean started) {
        this.rounds = rounds;
        this.time = time;
        this.started = started;
    }

    public int getRounds() {
        return rounds;
    }

    public void setRounds(int rounds) {
        this.rounds = rounds;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public boolean isStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }
}
