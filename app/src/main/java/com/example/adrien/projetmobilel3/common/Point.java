package com.example.adrien.projetmobilel3.common;

import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by MrkJudge on 24/02/2017.
 */

public class Point implements Parcelable {

    private float x;
    private float y;
    private int stroke;
    private int color;

    public Point(float x, float y, int stroke, int color) {
        this.x = x;
        this.y = y;
        this.stroke = stroke;
        this.color = color;
    }

    public Point(float x, float y) {
        this(x,y, Color.BLACK,1);
    }

    //TODO vérifier l'ordre de lecture/écriture dans le parcel
    public Point(Parcel parcel) {
        this.x = parcel.readFloat();
        this.y = parcel.readFloat();
        this.stroke = parcel.readInt();
        this.color = parcel.readInt();
    }

    public Parcelable.Creator<Point> CREATOR = new Parcelable.Creator<Point>() {
        @Override
        public Point createFromParcel(Parcel source) {
            return new Point(source);
        }

        @Override
        public Point[] newArray(int size) {
            return new Point[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(x);
        dest.writeFloat(y);
        dest.writeInt(stroke);
        dest.writeInt(color);
    }

    public int getColor() {
        return color;
    }
    public int getStroke() { return stroke;}
    public float getX() {
        return x;
    }
    public float getY() {
        return y;
    }

    public static int getByteLength() {
        return Float.SIZE *2 + Integer.SIZE *2;
    }

}
