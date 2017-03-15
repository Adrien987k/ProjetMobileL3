package com.example.adrien.projetmobilel3.draw;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Parcel;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.TreeMap;

/**
 * Created by MrkJudge on 24/01/2017.
 */

public class Draw extends   View {

    public static int color;
    public static final int DEFAULT_COLOR = Color.BLACK;

    public static int stroke;
    public static final int DEFAULT_STROKE = 1;

    private Paint paint = new Paint();
    private Path path = new Path();
    private Canvas myCanvas = new Canvas();
    private Bitmap bitMap;

    private Draw(Context context, AttributeSet attrs, int color, int stroke) {
        super(context,attrs);
        Draw.color = color;
        Draw.stroke = stroke;
        init();
    }

    public Draw(Context context, AttributeSet attrs) {
        this(context,attrs,DEFAULT_COLOR,DEFAULT_STROKE);
    }
    public Draw(Context context, AttributeSet attrs, Parcel parcel) {
        this(context,attrs,parcel.readInt(),parcel.readInt());
    }

    public Canvas getMyCanvas() {
        return myCanvas;
    }

    public void init() {
        paint.setAntiAlias(true);
        paint.setStrokeWidth(30);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
    }

    public synchronized void addEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                path.moveTo(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                myLineTo(x, y);
                break;
            case MotionEvent.ACTION_UP:
                myLineTo(x, y);
                myCanvas.drawPath(path, paint);
                path.reset();
                break;
        }
        postInvalidate();
    }

    private synchronized void myLineTo(float x, float y) {
        path.lineTo(x, y);
        myCanvas.drawPath(path,paint);
        postInvalidate();
        path.reset();
        path.moveTo(x,y);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        bitMap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        myCanvas = new Canvas(bitMap);
        myCanvas.drawColor(Color.WHITE);
        postInvalidate();

    }

    //TODO ordre incorrect, les points étrangers seront toujours dessinés par dessus
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        paint.setColor(Color.BLUE);
        paint.setStrokeWidth(30);

        canvas.drawBitmap(bitMap,0,0,paint);
        canvas.drawPath(path,paint);

    }

    public synchronized void clear() {
        myCanvas.drawColor(Color.WHITE);
    }
}
