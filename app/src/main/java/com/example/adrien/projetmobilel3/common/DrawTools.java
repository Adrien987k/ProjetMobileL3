package com.example.adrien.projetmobilel3.common;

import android.graphics.Paint;
import android.graphics.Path;

/**
 * The DrawTools class offers an interface to easily draw
 * on a canvas with the path and the paint contained here.
 * DrawTools is usually used to draw when you might have
 * multiple users drawing at the same time.
 */

public class DrawTools {

    /**
     * Path where points are added.
     */
    private Path path;

    /**
     * Paint called when the path need to be drawn.
     */
    private Paint paint;

    /**
     * Create a new DrawTools with an empty path and paint
     */
    public DrawTools() {
        this(new Path(),new Paint());
    }

    /**
     * Create a new DrawTools with the specified path and paint
     * @param path
     * @param paint
     */
    public DrawTools(Path path, Paint paint) {
        this.path = path;
        this.paint = paint;
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
    }

    public Path getPath() {
        return path;
    }

    public Paint getPaint() {
        return paint;
    }
}
