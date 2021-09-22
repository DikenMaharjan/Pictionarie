package com.example.pictionarie.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;

@SuppressLint("ViewConstructor")
public class AvailableColor extends View {

    float density;

    private static final String TAG = "AvailableColor";

    public int color;
    RectF borderRect;
    Paint borderPaint;
    int borderColor;

    public AvailableColor(Context context, int color) {
        super(context);
        this.color = color;

    }

    private void setBorderAttributes(int width, int height) {
        borderColor = Color.BLACK;
        borderPaint = new Paint();
        borderPaint.setStrokeWidth(2);
        borderPaint.setAntiAlias(true);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setColor(Color.BLACK);
        borderRect = new RectF(0, 0,width, height );

    }



    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        setBorderAttributes(w, h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(color);

        if (borderRect != null && borderPaint != null ){
            canvas.drawRect(borderRect, borderPaint);
        }
    }
}
