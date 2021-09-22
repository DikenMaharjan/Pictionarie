package com.example.pictionarie.model;

public class Score {
    long seconds;
    boolean answered;

    public Score(long seconds, boolean answered) {
        this.seconds = seconds;
        this.answered = answered;
    }

    public Score() {
    }

    public long getSeconds() {
        return seconds;
    }

    public void setSeconds(long seconds) {
        this.seconds = seconds;
    }

    public boolean isAnswered() {
        return answered;
    }

    public void setAnswered(boolean answered) {
        this.answered = answered;
    }
}
