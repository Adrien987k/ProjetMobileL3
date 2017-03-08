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

public class Draw extends View {

    private ArrayList<Point> points = new ArrayList<>();
    public static int color;
    public static final int DEFAULT_COLOR = Color.BLACK;

    public static int stroke;
    public static final int DEFAULT_STROKE = 1;

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



    public void addPoint(Point point) {
        points.add(point);
    }

    public ArrayList<Point> getPoints() {
        return points;
    }
    public void setPoints(ArrayList<Point> points) {
        this.points = points;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Paint paint = new Paint();
        for(Point point: points) {
            paint.setColor(point.getColor());
            paint.setStrokeWidth(point.getStroke());
            float x = point.getX();
            float y = point.getY();
            canvas.drawPoint(x,y,paint);
            paint.reset();
        }
    }
}
