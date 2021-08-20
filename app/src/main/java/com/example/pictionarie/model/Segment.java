package com.example.pictionarie.model;

import android.graphics.Path;

import java.util.ArrayList;
import java.util.List;

public class Segment {
    int color;
    int strokeWidth;
    List<Point> pointList = new ArrayList<>();
    Point fillColorPoint;
    int FillColor;

    public int getFillColor() {
        return FillColor;
    }

    public void setFillColor(int fillColor) {
        FillColor = fillColor;
    }

    public Segment(){}

    public Segment(int color, int strokeWidth, List<Point> pointList, Point fillColorPoint) {
        this.color = color;
        this.strokeWidth = strokeWidth;
        this.pointList = pointList;
        this.fillColorPoint = fillColorPoint;
    }

    public void setFillColorPoint(Point fillColorPoint){
        this.fillColorPoint = fillColorPoint;
    }
    public Point getFillColorPoint(){
        return fillColorPoint;
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

    public List<Point> getPointList() {
        return pointList;
    }

    public void setPointList(List<Point> pointList) {
        this.pointList = pointList;
    }
}
