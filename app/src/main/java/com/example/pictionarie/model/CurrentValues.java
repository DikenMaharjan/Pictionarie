package com.example.pictionarie.model;

public class CurrentValues {
    int color;
    int strokeWidth;

    public CurrentValues() {
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getStrokeWidth() {
        return strokeWidth;
    }

    public void setStrokeWidth(int strokeWidth) {
        this.strokeWidth = strokeWidth;
    }

    public CurrentValues(int color, int strokeWidth) {
        this.color = color;
        this.strokeWidth = strokeWidth;
    }
}

