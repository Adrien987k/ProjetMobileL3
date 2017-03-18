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

import com.example.adrien.projetmobilel3.activities.MainActivity;
import com.example.adrien.projetmobilel3.common.DrawTools;
import com.example.adrien.projetmobilel3.common.HardwareAddress;
import com.example.adrien.projetmobilel3.common.MyPath;
import com.example.adrien.projetmobilel3.common.PointPacket;

import java.util.ArrayList;
import java.util.TreeMap;

/**
 * Created by MrkJudge on 24/01/2017.
 */

public class Draw extends View {

    public int color;
    public static final int DEFAULT_COLOR = Color.BLACK;

    public int stroke;
    public static final int DEFAULT_STROKE = 10;

    public int alpha;
    public static final int DEFAULT_ALPHA = 255;

    private Paint paint;
    private Path path;
    private Canvas myCanvas = new Canvas();
    private Bitmap bitMap;
    private MyPath myPath;

    private MainActivity mainActivity;

    private ArrayList<PointPacket> points = new ArrayList<>();

    private Draw(Context context, AttributeSet attrs, int color, int stroke, int alpha) {
        super(context,attrs);
        this.color = color;
        this.stroke = stroke;
        this.alpha = alpha;
        this.paint = new Paint();
        this.path = new Path();
        init();
    }

    public Draw(Context context, AttributeSet attrs) {
        this(context,attrs,DEFAULT_COLOR,DEFAULT_STROKE,DEFAULT_ALPHA);
    }

    public Canvas getMyCanvas() {
        return myCanvas;
    }

    public void init() {
        paint.setAntiAlias(true);
        paint.setColor(color);
        paint.setStrokeWidth(stroke);
        paint.setAlpha(alpha);
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
                paint.setColor(color);
                paint.setStrokeWidth(stroke);
                paint.setAlpha(alpha);
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

    public synchronized void addPointPacket(PointPacket pointPacket, ArrayList<MyPath> paths) {

        if(paths == null)System.out.println(pointPacket);
        float x = pointPacket.getPoint().getX();
        float y = pointPacket.getPoint().getY();
        int color = pointPacket.getPoint().getColor();
        int stroke = pointPacket.getPoint().getStroke();

        switch (pointPacket.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if(paths != null) {
                    myPath = new MyPath();
                    myPath.addPointPacket(pointPacket);
                }
                path.moveTo(x, y);
                paint.setColor(color);
                paint.setStrokeWidth(stroke);
                paint.setAlpha(alpha);
                break;
            case MotionEvent.ACTION_MOVE:
                if(paths != null) {
                    myPath.addPointPacket(pointPacket);
                }
                myLineTo(x, y);
                break;
            case MotionEvent.ACTION_UP:
                myLineTo(x, y);
                myCanvas.drawPath(path, paint);
                if(paths != null) {
                    myPath.addPointPacket(pointPacket);
                    paths.add(myPath);
                }
                path.reset();
                break;
        }
        postInvalidate();
    }

    public synchronized void drawPointPacket(DrawTools drawTools, PointPacket pointPacket) {
        float x = pointPacket.getPoint().getX();
        float y = pointPacket.getPoint().getY();
        Path path = drawTools.getPath();
        Paint paint = drawTools.getPaint();

        switch (pointPacket.getAction()) {
            case MotionEvent.ACTION_DOWN:
                paint.setStrokeWidth(pointPacket.getPoint().getStroke());
                paint.setColor(pointPacket.getPoint().getColor());
                path.moveTo(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                myLineTo(x, y, drawTools);
                break;
            case MotionEvent.ACTION_UP:
                myLineTo(x, y, drawTools);
                path.reset();
                break;
        }
    }

    public synchronized void myLineTo(float x, float y, DrawTools drawTools) {
        Path path = drawTools.getPath();
        Paint paint = drawTools.getPaint();

        path.lineTo(x, y);
        getMyCanvas().drawPath(path, paint);
        postInvalidate();
        path.reset();
        path.moveTo(x,y);
    }

    private synchronized void myLineTo(float x, float y) {
        path.lineTo(x, y);
        myCanvas.drawPath(path,paint);
        postInvalidate();
        path.reset();
        path.moveTo(x,y);
    }

    public void drawPaths(ArrayList<MyPath> paths) {
        for(MyPath path: paths) {
            for(PointPacket pointPacket: path.getPointPackets()) {
                addPointPacket(pointPacket,null);
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        bitMap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        myCanvas = new Canvas(bitMap);
        myCanvas.drawColor(Color.WHITE);
        postInvalidate();
        if(mainActivity != null)
            mainActivity.sizeChangedDraw();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawBitmap(bitMap,0,0,paint);
        canvas.drawPath(path,paint);

    }

    public void setMainActivity(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public synchronized void clear() {

        mainActivity.getPaths().clear();
        myCanvas.drawColor(Color.WHITE);
    }

    public ArrayList<PointPacket> getPoints() {
        return points;
    }
    public void setPoints(ArrayList<PointPacket> points) {
        this.points = points;
        for(PointPacket pointPacket: points)
            drawPointPacket(getMainActivity().getUsers().get(pointPacket.getHardwareAddress()),pointPacket);
    }

    public MainActivity getMainActivity() {
        return mainActivity;
    }

}
