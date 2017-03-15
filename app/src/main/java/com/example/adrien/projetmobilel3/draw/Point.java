package com.example.adrien.projetmobilel3.draw;

import android.os.Parcel;
import android.os.Parcelable;

import java.nio.ByteBuffer;

/**
 * Created by MrkJudge on 24/02/2017.
 */

public class Point implements Parcelable {

    public static final int BYTES = (Float.SIZE / Byte.SIZE) * 2 + (Integer.SIZE / Byte.SIZE) *2;

    private float x;
    private float y;
    private int stroke;
    private int color;

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

    public Point(float x, float y, int stroke, int color) {
        this.x = x;
        this.y = y;
        this.stroke = stroke;
        this.color = color;
    }

    public Point(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        this.x = buffer.getFloat();
        this.y = buffer.getFloat();
        this.stroke = buffer.getInt();
        this.color = buffer.getInt();
    }

    private Point(Parcel parcel) {
        this.x = parcel.readFloat();
        this.y = parcel.readFloat();
        this.stroke = parcel.readInt();
        this.color = parcel.readInt();
    }

    public float getX() { return x; }
    public float getY() { return y; }
    public int getStroke() { return stroke; }
    public int getColor() { return color; }

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

    public byte[] getBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(BYTES);

        buffer.putFloat(x);
        buffer.putFloat(y);
        buffer.putInt(stroke);
        buffer.putInt(color);

        return buffer.array();
    }

    @Override
    public String toString() {
        return "(" + x + "," + y + ")" + " stroke: " + stroke + " color: " + color;
    }

}
