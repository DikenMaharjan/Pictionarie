package com.example.pictionarie.model;

public class Player {
    public boolean answered;
    String name;
    public boolean disconnected = false;
    int score;
    public boolean host;
    int turn;

    public Player(){
    }

    public void setTurn(int turn) {
        this.turn = turn;
    }

    public int getTurn(){
        return turn;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public Player(String name, boolean host, int turn) {
        this.name = name;
        this.host = host;
        score = 0;
        this.turn = turn;
        answered = false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
