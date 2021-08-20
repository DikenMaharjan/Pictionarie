package com.example.pictionarie.model;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import java.util.LinkedList;
import java.util.Queue;

import static android.content.ContentValues.TAG;

public class FillColor {
    protected Bitmap image = null;


    protected int width = 0;
    protected int height = 0;
    protected int[] pixels = null;
    protected int fillColor = 0;
    protected int startColor;
    protected boolean[] pixelsChecked;
    protected Queue<FloodFillRange> ranges;


    public FillColor(Bitmap img, int targetColor, int newColor) {
        useImage(img);
        setFillColor(newColor);
        setTargetColor(targetColor);
    }

    public void setTargetColor(int targetColor) {
        startColor = targetColor;
    }


    public void setFillColor(int value) {
        fillColor = value;
    }

    public void useImage(Bitmap img) {
        width = img.getWidth();
        height = img.getHeight();
        image = img;

        pixels = new int[width * height];

        image.getPixels(pixels, 0, width, 1, 1, width-1 , height -1);
    }

    protected void prepare() {
        pixelsChecked = new boolean[pixels.length];
        ranges = new LinkedList<>();
    }

    public void floodFill(int x, int y) {
        prepare();

        LinearFill(x, y);
        FloodFillRange range;

        while (ranges.size() > 0) {
            range = ranges.remove();

            int downPxIdx = (width * (range.Y + 1)) + range.startX;
            int upPxIdx = (width * (range.Y - 1)) + range.startX;
            int upY = range.Y - 1;
            int downY = range.Y + 1;

            for (int i = range.startX; i <= range.endX; i++) {
                if (range.Y > 0 && (!pixelsChecked[upPxIdx])
                        && CheckPixel(upPxIdx))
                    LinearFill(i, upY);
                if (range.Y < (height - 1) && (!pixelsChecked[downPxIdx])
                        && CheckPixel(downPxIdx))
                    LinearFill(i, downY);

                downPxIdx++;
                upPxIdx++;
            }
        }

        image.setPixels(pixels, 0, width, 1, 1, width - 1, height - 1);
    }

    protected void LinearFill(int x, int y) {
        int lFillLoc = x;
        int pxIdx = (width * y) + x;

        do {
            pixels[pxIdx] = fillColor;
            pixelsChecked[pxIdx] = true;
            lFillLoc--;
            pxIdx--;

        } while (lFillLoc >= 0 && (!pixelsChecked[pxIdx]) && CheckPixel(pxIdx));

        lFillLoc++;

        int rFillLoc = x;

        pxIdx = (width * y) + x;

        do {
            pixels[pxIdx] = fillColor;
            pixelsChecked[pxIdx] = true;
            rFillLoc++;
            pxIdx++;
        } while (rFillLoc < width && !pixelsChecked[pxIdx] && CheckPixel(pxIdx));

        rFillLoc--;

        FloodFillRange r = new FloodFillRange(lFillLoc, rFillLoc, y);

        ranges.offer(r);
    }

    protected boolean CheckPixel(int px) {


        return pixels[px] == startColor;

    }

    protected static class FloodFillRange {
        public int startX;
        public int endX;
        public int Y;

        public FloodFillRange(int startX, int endX, int y) {
            this.startX = startX;
            this.endX = endX;
            this.Y = y;
        }
    }
}