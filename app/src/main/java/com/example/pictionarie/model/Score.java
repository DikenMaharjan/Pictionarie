package com.example.pictionarie.model;

public class Score {
    String name;
    boolean answered;
    int score;
    boolean drawing;
    String id;

    public Score() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Score(String name, String id, boolean answered, int score, boolean drawing) {
        this.name = name;
        this.answered = answered;
        this.score = score;
        this.drawing = drawing;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isAnswered() {
        return answered;
    }

    public void setAnswered(boolean answered) {
        this.answered = answered;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public boolean isDrawing() {
        return drawing;
    }

    public void setDrawing(boolean drawing) {
        this.drawing = drawing;
    }
}
