package com.example.pictionarie.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.pictionarie.Server;
import com.example.pictionarie.model.CurrentValues;
import com.example.pictionarie.model.FillColor;
import com.example.pictionarie.model.Point;
import com.example.pictionarie.model.Segment;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

@SuppressLint("ViewConstructor")
public class DrawWindow extends View {
    private static final String TAG = "DrawWindow";
    final int STROKING_SCALE = 500;
    final int MOVE_LENGTH = 4;
    final int BACKGROUND_COLOR = Color.rgb(0xF0, 0xF0, 0xFF);
    public boolean pencilSelected;
    public boolean eraserSelected;
    public boolean fillColorSelected;
    public boolean pencilSizeShowing = false;
    public boolean drawing;
    int density = getResources().getDisplayMetrics().densityDpi;
    List<Segment> allSegments = new ArrayList<>();
    Segment currentSegment;
    int currentStrokeWidth;
    List<Point> currentPointsList = new ArrayList<>();
    Path pathToTrackCurrentTrack;
    Path pathForShowingPencilSize;
    Rect backgroundBorderRect;
    int LastX;
    int LastY;
    int height;
    int width;
    Canvas mBuffer;
    Bitmap mBitmap;
    float centreOfShowingPencilSize;
    ChildEventListener segmentsListener;
    ValueEventListener currentSegmentValuesListener;
    ChildEventListener currentSegmentPointsListener;
    ValueEventListener clearListener;
    ValueEventListener heightListener;
    ValueEventListener widthListener;
    ValueEventListener undoListener;
    private int currentColor;
    private Paint paintForDrawingCurrentPath;
    private Paint paintForDrawingCurrentPoints;
    private int drawerHeight;
    private int drawerWidth;
    public boolean enabled;


    public DrawWindow(Context context, boolean drawing) {
        super(context);
        this.drawing = drawing;

        currentColor = Color.BLACK;
        currentStrokeWidth = 5;
        selectPencil();

        enabled = false;

        centreOfShowingPencilSize = (float) 200 / (STROKING_SCALE * 2) * density + 10;
        pathForShowingPencilSize = new Path();
        pathForShowingPencilSize.moveTo(centreOfShowingPencilSize, centreOfShowingPencilSize);
        pathForShowingPencilSize.lineTo(centreOfShowingPencilSize, centreOfShowingPencilSize);

        paintForDrawingCurrentPath =

                getPaint(currentColor, currentStrokeWidth);

        pathToTrackCurrentTrack = new Path();
        currentSegmentValuesListener = new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    CurrentValues currentValues = snapshot.getValue(CurrentValues.class);
                    assert currentValues != null;
                    paintForDrawingCurrentPoints = getPaint(currentValues.getColor(),
                            currentValues.getStrokeWidth());
                } else {
                    currentPointsList.clear();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        segmentsListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Segment newSegment = snapshot.getValue(Segment.class);
                assert newSegment != null;
                Point fillColorPoint = newSegment.getFillColorPoint();
                if (fillColorPoint != null) {
                    fillColorPoint = scaledPoint(fillColorPoint);
                    floodFill(mBitmap, fillColorPoint, newSegment.getFillColor(),
                            newSegment.getColor());
                }
                if (!newSegment.getPointList().isEmpty()) {
                    Path path = getPathForPoints(newSegment.getPointList());
                    Paint paint = getPaint(newSegment.getColor(), newSegment.getStrokeWidth());
                    mBuffer.drawPath(path, paint);
                }
                allSegments.add(newSegment);
                invalidate();
            }


            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String
                    previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        currentSegmentPointsListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Point point = snapshot.getValue(Point.class);
                currentPointsList.add(point);
                Path path = getPathForPoints(currentPointsList);
                mBuffer.drawPath(path, paintForDrawingCurrentPoints);
                invalidate();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                currentPointsList.clear();

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        clearListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue(Boolean.class)) {
                    clear();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        undoListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue(Boolean.class)) {
                    undo();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        heightListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                drawerHeight = snapshot.getValue(Integer.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        widthListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                drawerWidth = snapshot.getValue(Integer.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

    }

    public void startReceiving() {
        if(enabled) {
            mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            mBuffer = new Canvas(mBitmap);
            mBuffer.drawColor(BACKGROUND_COLOR);
            drawing = false;
            Server.getDrawingRef().child("CurrentSegmentValues").addValueEventListener(currentSegmentValuesListener);
            Server.getDrawingRef().child("CurrentSegmentPoints").addChildEventListener(currentSegmentPointsListener);
            Server.getDrawingRef().child("Clear").addValueEventListener(clearListener);
            Server.getDrawingRef().child("Undo").addValueEventListener(undoListener);
            Server.getDrawingRef().child("Height").addListenerForSingleValueEvent(heightListener);
            Server.getDrawingRef().child("Width").addListenerForSingleValueEvent(widthListener);
            Server.getDrawingRef().child(Server.SEGMENTS_KEY).addChildEventListener(segmentsListener);
        }
    }

    public Path getPathForPoints(List<Point> points) {
        if (enabled) {
            Path path = new Path();
            Point current = points.get(0);
            if (!drawing) {
                current = scaledPoint(current);
            }
            path.moveTo(Math.round(current.x), Math.round(current.y));
            Point next = null;
            for (int i = 1; i < points.size(); ++i) {
                next = points.get(i);
                next = scaledPoint(next);
                path.quadTo(
                        current.x, current.y,
                        (next.x + current.x) / (float) 2, (next.y + current.y) / (float) 2
                );
                current = next;
            }
            if (next != null) {
                path.lineTo(next.x, next.y);
            }
            return path;
        }else{
            return null;
        }
    }

    public void selectPencil() {
        if (enabled) {
            pencilSelected = true;
            eraserSelected = false;
            fillColorSelected = false;
            paintForDrawingCurrentPath = getPaint(currentColor, currentStrokeWidth);
            invalidate();
        }

    }

    public void selectColor(int color) {
        if (enabled) {
            currentColor = color;
            paintForDrawingCurrentPath = getPaint(currentColor, currentStrokeWidth);
        }
    }

    public void changeStrokeWidth(int width) {
        if (enabled) {
            currentStrokeWidth = width;
            if (eraserSelected) {
                paintForDrawingCurrentPath = getPaint(BACKGROUND_COLOR, currentStrokeWidth);
            } else if (pencilSelected) {
                paintForDrawingCurrentPath = getPaint(currentColor, currentStrokeWidth);
            }
            invalidate();
        }
    }

    public void selectEraser() {
        if (enabled) {
            pencilSelected = false;
            eraserSelected = true;
            fillColorSelected = false;
            paintForDrawingCurrentPath = getPaint(BACKGROUND_COLOR, currentStrokeWidth);
        }
    }

    public void selectFillColor() {
        if (enabled) {
            pencilSelected = false;
            eraserSelected = false;
            fillColorSelected = true;
            invalidate();
        }
    }
    public void renewBoard(){
        mBuffer.drawColor(BACKGROUND_COLOR);
        allSegments.clear();
        invalidate();
        Server.getDrawingRef().child(Server.SEGMENTS_KEY).removeValue();
    }

    public void clear() {
            if (drawing) {
                Server.getDrawingRef().child("Clear").setValue(true);
                Server.getDrawingRef().child(Server.SEGMENTS_KEY).removeValue();
            }
            mBuffer.drawColor(BACKGROUND_COLOR);
            allSegments.clear();
            invalidate();
            if (drawing) {
                Server.getDrawingRef().child("Clear").setValue(false);
            }
    }

    public void undo() {
        if (enabled) {
            if (drawing) {
                Server.getDrawingRef().child("Undo").setValue(true);
            }
            Bitmap bufferBitmapForUndo = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas bufferCanvasForUndo = new Canvas(bufferBitmapForUndo);

            if (!allSegments.isEmpty()) {
                allSegments.remove(allSegments.size() - 1);
            }
            bufferCanvasForUndo.drawColor(BACKGROUND_COLOR);
            for (Segment segment : allSegments) {
                Point fillColorPoint = segment.getFillColorPoint();
                if (fillColorPoint != null) {
                    if (!drawing) {
                        fillColorPoint = scaledPoint(fillColorPoint);
                    }
                    floodFill(bufferBitmapForUndo, fillColorPoint, segment.getFillColor(), segment.getColor());
                    continue;
                }
                if (!segment.getPointList().isEmpty()) {
                    Path path = getPathForPoints(segment.getPointList());
                    Paint paint = getPaint(segment.getColor(), segment.getStrokeWidth());
                    bufferCanvasForUndo.drawPath(path, paint);
                }
            }
            mBuffer = bufferCanvasForUndo;
            mBitmap = bufferBitmapForUndo;
            invalidate();
            if (drawing) {
                Server.getDrawingRef().child("Undo").setValue(false);

            }
        }
    }

    public void floodFill(Bitmap image, Point node, int targetColor,
                          int replacementColor) {
        if (enabled) {
            FillColor temp = new FillColor(image, targetColor,
                    replacementColor);
            temp.floodFill(node.x, node.y);
        }
    }

    public Paint getPaint(int currentColor, int width) {
            Paint mPaint = new Paint();
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeCap(Paint.Cap.ROUND);
            mPaint.setStrokeJoin(Paint.Join.ROUND);
            mPaint.setColor(currentColor);
            mPaint.setStrokeWidth(((float) width / STROKING_SCALE) * density);
            return mPaint;
    }

    public void startDrawing() {
        if (enabled) {
            mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            mBuffer = new Canvas(mBitmap);
            mBuffer.drawColor(BACKGROUND_COLOR);
            Server.getDrawingRef().child("CurrentSegmentValues").removeEventListener(currentSegmentValuesListener);
            Server.getDrawingRef().child("CurrentSegmentPoints").removeEventListener(currentSegmentPointsListener);
            Server.getDrawingRef().child("Clear").removeEventListener(clearListener);
            Server.getDrawingRef().child("Undo").removeEventListener(undoListener);
            Server.getDrawingRef().child("Height").removeEventListener(heightListener);
            Server.getDrawingRef().child("Width").removeEventListener(widthListener);
            Server.getDrawingRef().child(Server.SEGMENTS_KEY).removeEventListener(segmentsListener);
            Server.getDrawingRef().child("Height").setValue(height);
            Server.getDrawingRef().child("Width").setValue(width);
            Server.getDrawingRef().child("Undo").setValue(false);
            Server.getDrawingRef().child("Clear").setValue(false);
            drawing = true;
            invalidate();
        }
    }


    private Paint getPaintForBorderEraser() {
        if (enabled) {
            Paint paint = new Paint();
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setColor(Color.BLACK);
            paint.setStrokeWidth(0);
            paint.setStyle(Paint.Style.STROKE);
            return paint;
        }else{
            return null;
        }

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        height = h;
        width = w;
        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mBuffer = new Canvas(mBitmap);
        mBuffer.drawColor(BACKGROUND_COLOR);

        backgroundBorderRect = new Rect(0, 0, w, h);

    }

    public void disable(){
        enabled = false;
        Server.getDrawingRef().child("CurrentSegmentValues").removeEventListener(currentSegmentValuesListener);
        Server.getDrawingRef().child("CurrentSegmentPoints").removeEventListener(currentSegmentPointsListener);
        Server.getDrawingRef().child("Clear").removeEventListener(clearListener);
        Server.getDrawingRef().child("Undo").removeEventListener(undoListener);
        Server.getDrawingRef().child("Height").removeEventListener(heightListener);
        Server.getDrawingRef().child("Width").removeEventListener(widthListener);
        Server.getDrawingRef().child(Server.SEGMENTS_KEY).removeEventListener(segmentsListener);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(mBitmap, 0, 0, null);
            if (drawing) {
                if (mBitmap != null && mBuffer != null) {
                    if (pencilSelected) {
                        canvas.drawBitmap(mBitmap, 0, 0, null);
                        canvas.drawPath(pathToTrackCurrentTrack, paintForDrawingCurrentPath);

                    } else if (eraserSelected) {
                        canvas.drawBitmap(mBitmap, 0, 0, null);
                        canvas.drawPath(pathToTrackCurrentTrack, paintForDrawingCurrentPath);
                        canvas.drawCircle(LastX, LastY,
                                (float) currentStrokeWidth / STROKING_SCALE * density / 2, getPaintForBorderEraser());

                    } else if (fillColorSelected) {
                        canvas.drawBitmap(mBitmap, 0, 0, null);
                    }

                    canvas.drawRect(backgroundBorderRect, getPaint(Color.BLACK, 20));

                }
                if (pencilSizeShowing) {
                    if (eraserSelected || pencilSelected) {
                        canvas.drawPath(pathForShowingPencilSize, paintForDrawingCurrentPath);
                        canvas.drawCircle(centreOfShowingPencilSize, centreOfShowingPencilSize,
                                (float) currentStrokeWidth / STROKING_SCALE * density / 2,
                                getPaintForBorderEraser());
                    }
                }
            }

            canvas.drawRect(backgroundBorderRect, getPaint(Color.BLACK, 20));




    }

    private void onTouchStart(int x, int y) {
        if (pencilSelected) {
            pathToTrackCurrentTrack = new Path();
            pathToTrackCurrentTrack.moveTo(x, y);
            LastX = x;
            LastY = y;
            currentSegment = new Segment(currentColor, currentStrokeWidth, new ArrayList<>(), null);
            currentSegment.getPointList().add(new Point(x, y));
            Server.getDrawingRef().child("CurrentSegmentValues").setValue(new CurrentValues(currentColor, currentStrokeWidth));
            Server.getDrawingRef().child("CurrentSegmentPoints").push().setValue(new Point(x, y));

        } else if (eraserSelected) {
            pathToTrackCurrentTrack = new Path();
            pathToTrackCurrentTrack.moveTo(x, y);
            LastX = x;
            LastY = y;
            currentSegment = new Segment(BACKGROUND_COLOR, currentStrokeWidth, new ArrayList<>(), null);
            currentSegment.getPointList().add(new Point(x, y));
            Server.getDrawingRef().child("CurrentSegmentValues").setValue(new CurrentValues(BACKGROUND_COLOR, currentStrokeWidth));
            Server.getDrawingRef().child("CurrentSegmentPoints").push().setValue(new Point(x, y));

        } else if (fillColorSelected) {
            Segment fillColorSegment = new Segment();
            fillColorSegment.setColor(currentColor);
            fillColorSegment.setFillColor(mBitmap.getPixel(x, y));
            fillColorSegment.setFillColorPoint(new Point(x, y));
            allSegments.add(fillColorSegment);
            Server.getDrawingRef().child(Server.SEGMENTS_KEY).push().setValue(fillColorSegment);
            floodFill(mBitmap, new Point(x, y), mBitmap.getPixel(x, y), currentColor);

        }
    }

    private void onTouchMove(int x, int y) {
        if (pencilSelected || eraserSelected) {
            int dx = Math.abs(LastX - x);
            int dy = Math.abs(LastY - y);
            if (dx > MOVE_LENGTH || dy > MOVE_LENGTH) {
                pathToTrackCurrentTrack.quadTo(LastX, LastY, (float) (LastX + x) / 2,
                        (float) (LastY + y) / 2);
                LastX = x;
                LastY = y;
                currentSegment.getPointList().add(new Point(LastX, LastY));
                Server.getDrawingRef().child("CurrentSegmentPoints").push().setValue(new Point(LastX, LastY));
            }
        }

    }

    private void onTouchEnd(int x, int y) {
        if (pencilSelected || eraserSelected) {
            pathToTrackCurrentTrack.lineTo(x, y);
            mBuffer.drawPath(pathToTrackCurrentTrack, paintForDrawingCurrentPath);
            currentSegment.getPointList().add(new Point(x, y));
            Server.getDrawingRef().child("CurrentSegmentPoints").push().setValue(new Point(x, y));
            Server.getDrawingRef().child("CurrentSegmentValues").removeValue();
            Server.getDrawingRef().child("CurrentSegmentPoints").removeValue();
            currentPointsList.clear();
            allSegments.add(currentSegment);
            Server.getDrawingRef().child(Server.SEGMENTS_KEY).push().setValue(currentSegment);
            pathToTrackCurrentTrack.reset();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (enabled) {
            if (drawing) {
                int x = (int) event.getX();
                int y = (int) event.getY();

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        onTouchStart(x, y);
                        invalidate();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        onTouchMove(x, y);
                        invalidate();
                        break;
                    case MotionEvent.ACTION_UP:
                        onTouchEnd(x, y);
                        invalidate();
                        break;
                }
            }
        }
        return  true;
    }

    private Point scaledPoint(Point initPoint) {
        if (enabled) {
            if (!drawing) {
                return new Point(Math.round(initPoint.x * (float) width / drawerWidth),
                        Math.round(initPoint.y * (float) height / drawerHeight));
            } else {
                return initPoint;
            }
        }else{
            return null;
        }
    }

}