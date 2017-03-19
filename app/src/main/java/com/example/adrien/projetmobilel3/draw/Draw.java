package com.example.adrien.projetmobilel3.draw;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.example.adrien.projetmobilel3.activities.DrawActivity;
import com.example.adrien.projetmobilel3.common.DrawTools;
import com.example.adrien.projetmobilel3.common.MyPath;
import com.example.adrien.projetmobilel3.common.PointPacket;

import java.util.ArrayList;

/**
 * The Draw class is a view where users will draw.
 * A listener must be added to catch user's action
 * and call Draw methods to actually draw.
 */
public class Draw extends View {

    /**
     * The current color.
     * When a point is drawn by the local user, his color will be this one.
     */
    public int color;

    /**
     * The default color at starting.
     */
    public static final int DEFAULT_COLOR = Color.BLACK;

    /**
     * The current stroke.
     * When a point is drawn by the local user, his stroke will be this one.
     */
    public int stroke;

    /**
     * The default stroke at starting.
     */
    public static final int DEFAULT_STROKE = 10;

    /**
     * The alpha transparency.
     * When a point is drawn by the local user, his transparency will be this one.
     * Note that this feature is not fully yet implemented and alpha cannot be changed.
     */
    public int alpha;

    /**
     * The alpha transparency at starting.
     */
    public static final int DEFAULT_ALPHA = 255;

    /**
     * The paint of the local user, mainly containing the color and the stroke.
     * This paint is used when you need to re-draw.
     */
    private Paint paint;

    /**
     * The path of the local user.
     * When a point is draw, he will be added to the path then the whole path
     * will be drawn.
     */
    private Path path;

    /**
     * The canvas where everything is drawn.
     * Each path from every users is drawn on this canvas,
     * then the canvas is drawn on the real canvas in onDraw method;
     * Usually the onDraw method is immediately called when a path is modified.
     */
    private Canvas myCanvas = new Canvas();

    /**
     * The bitmap of the canvas.
     */
    private Bitmap bitMap;

    /**
     * A path containing every points drawn by the local user.
     * It's used to save points when the activity need to restart.
     * Note that this path will be frequently add on a path list and reset.
     */
    private MyPath myPath;

    /**
     * An access to the draw activity.
     */
    private DrawActivity drawActivity;

    /**
     * An array list containing every points drawn.
     * It's used to save points when the activity need to restart.
     * //TODO unused ?
     */
    private ArrayList<PointPacket> points = new ArrayList<>();

    /**
     * Create a draw view with the specified color, stroke and transparency.
     * @param color The color at starting.
     * @param stroke The stroke at starting.
     * @param alpha The alpha transparency at startgin.
     */
    private Draw(Context context, AttributeSet attrs, int color, int stroke, int alpha) {
        super(context,attrs);
        this.color = color;
        this.stroke = stroke;
        this.alpha = alpha;
        this.paint = new Paint();
        this.path = new Path();
        init();
    }

    /**
     * Create a draw view with default values of color, stroke and transparency.
     */
    public Draw(Context context, AttributeSet attrs) {
        this(context,attrs,DEFAULT_COLOR,DEFAULT_STROKE,DEFAULT_ALPHA);
    }

    public Canvas getMyCanvas() {
        return myCanvas;
    }

    /**
     * Initialize the paint with current color, stroke and transparency.
     * Also, it's setting some stroke parameters to draw simple stroke.
     */
    private void init() {
        paint.setAntiAlias(true);
        paint.setColor(color);
        paint.setStrokeWidth(stroke);
        paint.setAlpha(alpha);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
    }

    /**
     * Draw a point from the received event.
     * It extracts coordinates and draw the point with current drawing parameters.
     * @deprecated Note that it works only for local draw. If you have multiple users,
     * you should not use this method, because you can't identify the drawer.
     * @param event The MotionEvent received.
     */
    @Deprecated
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

    /**
     * Draw a point and add it to the path list.
     * @param pointPacket The point to draw.
     * @param paths The path list to add the point.
     */
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

    /**
     * Draw a point with the specified draw tools.
     * This method is used when you have multiple users
     * and each of them have a personal draw tools.
     * @param drawTools The draw tools to use.
     * @param pointPacket The point to draw.
     */
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

    /**
     * A personal implementation of the method Path.lineTo.
     * This method must be used instead of Path.lineTo if you have multiple users.
     * It will prevent you to have superposition conflicts,
     * because only the new point, and therefore the line to the last point, is drawn.
     * @param drawTools The draw tools to use.
     */
    private synchronized void myLineTo(float x, float y, DrawTools drawTools) {
        Path path = drawTools.getPath();
        Paint paint = drawTools.getPaint();

        path.lineTo(x, y);
        getMyCanvas().drawPath(path, paint);
        postInvalidate();
        path.reset();
        path.moveTo(x,y);
    }

    /**
     * A personal implementation of the method Path.lineTo.
     * This method must be used instead of Path.lineTo if you have multiple users.
     * It will prevent you to have superposition conflicts,
     * because only the new point, and therefore the line to the last point, is drawn.
     * @deprecated Note that it only works for local draw. If you have multiple users,
     * you should not use this method, because you can't identify the drawer.
     */
    @Deprecated
    private synchronized void myLineTo(float x, float y) {
        path.lineTo(x, y);
        myCanvas.drawPath(path,paint);
        postInvalidate();
        path.reset();
        path.moveTo(x,y);
    }

    /**
     * Draw all paths from the list.
     * This is called when the activity has restarted and saved points
     * need to be re-drawn.
     * @param paths
     */
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
        if(drawActivity != null)
            drawActivity.sizeChangedDraw();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawBitmap(bitMap,0,0,paint);
        canvas.drawPath(path,paint);

    }

    /**
     * Set the draw activity link.
     * This method must be called at the start of the activity
     * if you need to save points in the future.
     * @param drawActivity
     */
    public void setDrawActivity(DrawActivity drawActivity) {
        this.drawActivity = drawActivity;
    }

    /**
     * Clear all known points and clear the drawable area.
     */
    public synchronized void clear() {
        //drawActivity.getPaths().clear();
        myCanvas.drawColor(Color.WHITE);
    }

    public ArrayList<PointPacket> getPoints() {
        return points;
    }

    /**
     * Draw all points from the list.
     * Note that this method can handle multiple users.
     * @param points The point list to draw.
     */
    public void setPoints(ArrayList<PointPacket> points) {
        this.points = points;
        for(PointPacket pointPacket: points)
            drawPointPacket(getDrawActivity().getUsers().get(pointPacket.getHardwareAddress()),pointPacket);
    }

    public DrawActivity getDrawActivity() {
        return drawActivity;
    }

}
