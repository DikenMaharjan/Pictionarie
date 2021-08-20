package com.example.pictionarie.views;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;

import com.example.pictionarie.Server;
import com.example.pictionarie.model.CurrentValues;
import com.example.pictionarie.model.FillColor;
import com.example.pictionarie.model.Point;
import com.example.pictionarie.model.Segment;

import java.util.ArrayList;
import java.util.List;

import io.socket.emitter.Emitter;

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
    public boolean enabled;
    Context context;
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
    private int currentColor;
    private Paint paintForDrawingCurrentPath;
    private Paint paintForDrawingCurrentPoints;
    private int drawerHeight;
    private int drawerWidth;


    public DrawWindow(Context context) {
        super(context);
        this.context = context;


        currentColor = Color.BLACK;
        currentStrokeWidth = 5;
        selectPencil();

        enabled = false;

        centreOfShowingPencilSize = (float) 200 / (STROKING_SCALE * 2) * density + 10;
        pathForShowingPencilSize = new Path();
        pathForShowingPencilSize.moveTo(centreOfShowingPencilSize, centreOfShowingPencilSize);
        pathForShowingPencilSize.lineTo(centreOfShowingPencilSize, centreOfShowingPencilSize);

        paintForDrawingCurrentPath = getPaint(currentColor, currentStrokeWidth);

        pathToTrackCurrentTrack = new Path();

        Emitter.Listener currentSegmentValuesListener = args -> {
            CurrentValues currentValues = Server.gson.fromJson(args[0].toString(),
                    CurrentValues.class);
            paintForDrawingCurrentPoints = getPaint(currentValues.getColor(),
                    currentValues.getStrokeWidth());
        };


        Emitter.Listener segmentsListener = args -> {
            Segment newSegment = Server.gson.fromJson(args[0].toString(), Segment.class);
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
            ((Activity) context).runOnUiThread(this::invalidate);
        };

        Emitter.Listener currentSegmentPointsListener = args -> {
            Point point = Server.gson.fromJson(args[0].toString(), Point.class);
            currentPointsList.add(point);
            Path path = getPathForPoints(currentPointsList);
            mBuffer.drawPath(path, paintForDrawingCurrentPoints);
            ((Activity) context).runOnUiThread(this::invalidate);
        };

        Emitter.Listener clearListener = args -> ((Activity) context).runOnUiThread(this::clear);

        Emitter.Listener undoListener = args -> ((Activity) context).runOnUiThread(this::undo);

        Emitter.Listener heightListener = args -> drawerHeight = (int) args[0];

        Emitter.Listener widthListener = args -> drawerWidth = (int) args[0];
        Server.socket.on(Server.RECEIVER_UNDO, undoListener);
        Server.socket.on(Server.RECEIVER_CLEAR, clearListener);
        Server.socket.on(Server.RECEIVER_CURRENT_SEG_VALUES, currentSegmentValuesListener);
        Server.socket.on(Server.RECEIVER_CURRENT_SEG_POINTS, currentSegmentPointsListener);
        Server.socket.on(Server.RECEIVER_HEIGHT, heightListener);
        Server.socket.on(Server.RECEIVER_WIDTH, widthListener);
        Server.socket.on(Server.RECEIVER_SEGMENTS, segmentsListener);
        Server.socket.on(Server.RENEW_BOARD, args -> ((Activity) context).runOnUiThread(() -> {
            currentPointsList.clear();
            mBuffer.drawColor(BACKGROUND_COLOR);
            allSegments.clear();
            invalidate();
            pathToTrackCurrentTrack.reset();
        }));
        Server.socket.on(Server.RECEIVER_CLEAR_CURRENT_POINTS, args -> currentPointsList.clear());

    }

    public void startReceiving() {
        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mBuffer = new Canvas(mBitmap);
        mBuffer.drawColor(BACKGROUND_COLOR);
        drawing = false;
    }

    public Path getPathForPoints(List<Point> points) {
        Path path = new Path();
        Point current = points.get(0);
        if (!drawing) {
            current = scaledPoint(current);
        }
        assert current != null;
        path.moveTo(Math.round(current.x), Math.round(current.y));
        Point next = null;
        for (int i = 1; i < points.size(); ++i) {
            next = points.get(i);
            next = scaledPoint(next);
            assert next != null;
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
    }

    public void selectPencil() {
        pencilSelected = true;
        eraserSelected = false;
        fillColorSelected = false;
        paintForDrawingCurrentPath = getPaint(currentColor, currentStrokeWidth);
        ((Activity) context).runOnUiThread(this::invalidate);


    }

    public void selectColor(int color) {
        currentColor = color;
        paintForDrawingCurrentPath = getPaint(currentColor, currentStrokeWidth);

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
        pencilSelected = false;
        eraserSelected = true;
        fillColorSelected = false;
        paintForDrawingCurrentPath = getPaint(BACKGROUND_COLOR, currentStrokeWidth);
    }

    public void selectFillColor() {
        pencilSelected = false;
        eraserSelected = false;
        fillColorSelected = true;
        invalidate();

    }


    public void clear() {
        if (drawing) {
            Server.socket.emit(Server.DRAWER_CLEAR);
        }
        mBuffer.drawColor(BACKGROUND_COLOR);
        allSegments.clear();
        invalidate();
    }

    public void undo() {
        if (enabled) {
            if (drawing) {
                Server.socket.emit(Server.DRAWER_UNDO);
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
        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mBuffer = new Canvas(mBitmap);
        mBuffer.drawColor(BACKGROUND_COLOR);
        Server.socket.emit(Server.DRAWER_HEIGHT, height);
        Server.socket.emit(Server.DRAWER_WIDTH, width);
        drawing = true;
        ((Activity) context).runOnUiThread(this::invalidate);
    }


    private Paint getPaintForBorderEraser() {

            Paint paint = new Paint();
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setColor(Color.BLACK);
            paint.setStrokeWidth(0);
            paint.setStyle(Paint.Style.STROKE);
            return paint;


    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        height = h;
        width = w;
        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mBuffer = new Canvas(mBitmap);
        mBuffer.drawColor(BACKGROUND_COLOR);

        backgroundBorderRect = new Rect(0, 0, w, h);
        if (drawing) {
            Server.socket.emit(Server.DRAWER_HEIGHT, h);
            Server.socket.emit(Server.DRAWER_WIDTH, w);
        }

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
                    if (enabled) {
                        canvas.drawCircle(LastX, LastY,
                                (float) currentStrokeWidth / STROKING_SCALE * density / 2, getPaintForBorderEraser());
                    }

                } else if (fillColorSelected) {
                    canvas.drawBitmap(mBitmap, 0, 0, null);
                }

                canvas.drawRect(backgroundBorderRect, getPaint(Color.BLACK, 20));

            }
            if (enabled) {
                if (pencilSizeShowing) {
                    if (eraserSelected || pencilSelected) {
                        canvas.drawPath(pathForShowingPencilSize, paintForDrawingCurrentPath);
                        canvas.drawCircle(centreOfShowingPencilSize, centreOfShowingPencilSize,
                                (float) currentStrokeWidth / STROKING_SCALE * density / 2,
                                getPaintForBorderEraser());
                    }
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
            Server.socket.emit(Server.DRAWER_CURRENT_SEG_VALUES,
                    Server.gson.toJson(new CurrentValues(currentColor, currentStrokeWidth)));
            Server.socket.emit(Server.DRAWER_CURRENT_SEG_POINTS, Server.gson.toJson(new Point(x, y)));

        } else if (eraserSelected) {
            pathToTrackCurrentTrack = new Path();
            pathToTrackCurrentTrack.moveTo(x, y);
            LastX = x;
            LastY = y;
            currentSegment = new Segment(BACKGROUND_COLOR, currentStrokeWidth, new ArrayList<>(), null);
            currentSegment.getPointList().add(new Point(x, y));
            Server.socket.emit(Server.DRAWER_CURRENT_SEG_VALUES,
                    Server.gson.toJson(new CurrentValues(BACKGROUND_COLOR, currentStrokeWidth)));
            Server.socket.emit(Server.DRAWER_CURRENT_SEG_POINTS, Server.gson.toJson(new Point(x, y)));

        } else if (fillColorSelected) {
            Segment fillColorSegment = new Segment();
            fillColorSegment.setColor(currentColor);
            fillColorSegment.setFillColor(mBitmap.getPixel(x, y));
            fillColorSegment.setFillColorPoint(new Point(x, y));
            allSegments.add(fillColorSegment);
            Server.socket.emit(Server.DRAWER_SEGMENTS, Server.gson.toJson(fillColorSegment));
            floodFill(mBitmap, new Point(x, y), mBitmap.getPixel(x, y), currentColor);

        }
    }

    private void onTouchMove(int x, int y) {
        if (pathToTrackCurrentTrack != null && currentSegment != null) {
            if (pencilSelected || eraserSelected) {
                int dx = Math.abs(LastX - x);
                int dy = Math.abs(LastY - y);
                if (dx > MOVE_LENGTH || dy > MOVE_LENGTH) {
                    pathToTrackCurrentTrack.quadTo(LastX, LastY, (float) (LastX + x) / 2,
                            (float) (LastY + y) / 2);
                    LastX = x;
                    LastY = y;
                    currentSegment.getPointList().add(new Point(LastX, LastY));
                    Server.socket.emit(Server.DRAWER_CURRENT_SEG_POINTS,
                            Server.gson.toJson(new Point(LastX, LastY)));
                }
            }
        }
    }

    private void onTouchEnd(int x, int y) {
        if (pathToTrackCurrentTrack != null && paintForDrawingCurrentPath != null && currentSegment != null) {
            if (pencilSelected || eraserSelected) {
                pathToTrackCurrentTrack.lineTo(x, y);
                mBuffer.drawPath(pathToTrackCurrentTrack, paintForDrawingCurrentPath);
                currentSegment.getPointList().add(new Point(x, y));
                Server.socket.emit(Server.DRAWER_CURRENT_SEG_POINTS, Server.gson.toJson(new Point(x, y)));
                Server.socket.emit(Server.DRAWER_CLEAR_CURRENT_POINTS);
                currentPointsList.clear();
                allSegments.add(currentSegment);
                Server.socket.emit(Server.DRAWER_SEGMENTS, Server.gson.toJson(currentSegment
                ));
                pathToTrackCurrentTrack.reset();
            }
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
        return true;
    }

    private Point scaledPoint(Point initPoint) {
        if (!drawing) {
            return new Point(Math.round(initPoint.x * (float) width / drawerWidth),
                    Math.round(initPoint.y * (float) height / drawerHeight));
        } else {
            return initPoint;
        }
    }

    public void removeListeners() {
        Server.socket.off(Server.RECEIVER_UNDO);
        Server.socket.off(Server.RECEIVER_CLEAR);
        Server.socket.off(Server.RECEIVER_CURRENT_SEG_VALUES);
        Server.socket.off(Server.RECEIVER_CURRENT_SEG_POINTS);
        Server.socket.off(Server.RECEIVER_HEIGHT);
        Server.socket.off(Server.RECEIVER_WIDTH);
        Server.socket.off(Server.RECEIVER_SEGMENTS);
        Server.socket.off(Server.RENEW_BOARD);
        Server.socket.off(Server.RECEIVER_CLEAR_CURRENT_POINTS);
    }
}