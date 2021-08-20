package com.example.pictionarie.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;

@SuppressLint("ViewConstructor")
public class Tools extends AvailableColor {
    Bitmap bitmap;


    public Tools(Context context, Bitmap bitmap) {
        super(context, 0);
        this.bitmap = bitmap;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (bitmap != null) {
            canvas.drawBitmap(bitmap, (float) (getWidth() - bitmap.getWidth()) / 2,
                    (float) (getHeight() - bitmap.getHeight()) / 2,
                    null);
        }
    }
}
