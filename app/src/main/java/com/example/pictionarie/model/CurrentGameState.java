package com.example.pictionarie.model;

public class CurrentGameState {
    int round;
    int turn;
    String name;
    public CurrentGameState(){}
    public CurrentGameState(int round, int turn, String name){
        this.name = name;
        this.round = round;
        this.turn = turn;
    }

    public void setRound(int round){
        this.round = round;
    }
    public String getName(){
        return name;
    }
    public void setName(String name){
        this.name = name;
    }

    public void setTurn(int turn){
        this.turn = turn;
    }
    public int getRound (){
        return round;
    }
    public int getTurn (){
        return turn;
    }
}
