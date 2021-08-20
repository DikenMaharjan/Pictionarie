package com.example.pictionarie.model;


public class Messages {
    String name;
    String message;
    boolean correctAnswer;
    boolean firstTimeAnswer;
    boolean alreadyAnswered;

    public Messages() {
    }



    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(boolean correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public boolean isFirstTimeAnswer() {
        return firstTimeAnswer;
    }

    public void setFirstTimeAnswer(boolean firstTimeAnswer) {
        this.firstTimeAnswer = firstTimeAnswer;
    }

    public boolean isAlreadyAnswered() {
        return alreadyAnswered;
    }

    public void setAlreadyAnswered(boolean alreadyAnswered) {
        this.alreadyAnswered = alreadyAnswered;
    }

    public Messages(String name, String message, boolean correctAnswer, boolean firstTimeAnswer, boolean alreadyAnswered) {
        this.name = name;
        this.message = message;
        this.correctAnswer = correctAnswer;
        this.firstTimeAnswer = firstTimeAnswer;
        this.alreadyAnswered = alreadyAnswered;
    }
}
