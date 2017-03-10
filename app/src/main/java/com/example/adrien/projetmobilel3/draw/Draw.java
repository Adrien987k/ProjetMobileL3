package com.example.adrien.projetmobilel3.draw;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Parcel;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;

/**
 * Created by MrkJudge on 24/01/2017.
 */

public class Draw extends   View {

    private final ArrayList<Point> points = new ArrayList<>();
    public static int color;
    public static final int DEFAULT_COLOR = Color.BLACK;

    public static int stroke;
    public static final int DEFAULT_STROKE = 1;

    private final Runnable invalidateRun = new Runnable() {
        @Override
        public void run() {
            invalidate();
        }
    };

    private final Paint paint = new Paint();

    public Draw(Context context, AttributeSet attrs) {
        super(context,attrs);
        color = DEFAULT_COLOR;
        stroke = DEFAULT_STROKE;
    }
    public Draw(Context context, AttributeSet attrs, Parcel parcel) {
        super(context,attrs);
        color = parcel.readInt();
        stroke = parcel.readInt();
    }

    public synchronized void addPoint(Point point) {
        points.add(point);
        //postInvalidate();
        post(invalidateRun);
    }
    public synchronized void addAllPoints(ArrayList<Point> points) {
        points.addAll(points);
        //postInvalidate();
        post(invalidateRun);
    }

    public ArrayList<Point> getPoints() {
        return points;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        synchronized (canvas) {
            paint.setColor(Color.WHITE);
            canvas.drawColor(Color.WHITE);
            paint.reset();

            ArrayList<Point> knownPoints = new ArrayList<>(getPoints());
/*
            for (Point point : knownPoints) {
                paint.setColor(point.getColor());
                paint.setStrokeWidth(point.getStroke());
                float x = point.getX();
                float y = point.getY();
                canvas.drawPoint(x, y, paint);
                paint.reset();
            }
*/
            //TODO dessiner des traits qui ressemblent à quelque chose
            for(int i = 0; i < knownPoints.size()-1; i++) {
                Point point1 = knownPoints.get(i);
                Point point2 = knownPoints.get(i+1);

                float x1 = point1.getX();
                float y1 = point1.getY();
                float x2 = point2.getX();
                float y2 = point2.getY();

                paint.setColor(point1.getColor());
                paint.setStrokeWidth(point1.getStroke());
                canvas.drawCircle(x1,y1,paint.getStrokeWidth()/2,paint);

                if(point2.getFollower())
                    canvas.drawLine(x2,y2,x1,y1,paint);

                paint.setColor(point2.getColor());
                paint.setStrokeWidth(point2.getStroke());

                canvas.drawCircle(x2,y2,paint.getStrokeMiter(),paint);
            }

        }
    }

    public synchronized void clear() {
        getPoints().clear();
        post(invalidateRun);
    }
}
