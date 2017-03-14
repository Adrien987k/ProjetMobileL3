package com.example.adrien.projetmobilel3.common;

import android.graphics.Paint;
import android.graphics.Path;

import com.example.adrien.projetmobilel3.draw.Draw;

/**
 * Created by MrkJudge on 14/03/2017.
 */

public class DrawTools {
    private Path path;
    private Paint paint;

    public DrawTools() {
        this(new Path(),new Paint());
    }

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
