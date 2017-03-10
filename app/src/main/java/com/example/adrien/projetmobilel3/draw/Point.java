package com.example.adrien.projetmobilel3.draw;

import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;

import java.nio.ByteBuffer;

/**
 * Created by MrkJudge on 24/02/2017.
 */

public class Point implements Parcelable {

    private float x;
    private float y;
    private int stroke;
    private int color;
    private boolean follower = false;

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

    public Point(float x, float y, int stroke, int color, boolean follower) {
        this(x,y,stroke,color);
        this.follower = follower;
    }

    public Point(float x, float y) {
        this(x,y, Color.BLACK,1);
    }

    public Point(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        this.x = buffer.getFloat();
        this.y = buffer.getFloat();
        this.stroke = buffer.getInt();
        this.color = buffer.getInt();
        this.follower = byteToBoolean(buffer.get());
    }

    private Point(Parcel parcel) {
        this.x = parcel.readFloat();
        this.y = parcel.readFloat();
        this.stroke = parcel.readInt();
        this.color = parcel.readInt();
        this.follower = byteToBoolean(parcel.readByte());
    }

    public float getX() { return x; }
    public float getY() { return y; }
    public int getStroke() { return stroke; }
    public int getColor() { return color; }
    public boolean getFollower() { return follower; }

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
        dest.writeByte(booleanToByte(follower));
    }

    public static int getByteLength() {
        return Float.SIZE *2 + Integer.SIZE *2 + Byte.SIZE;
    }

    public byte[] getBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(getByteLength());

        buffer.putFloat(x);
        buffer.putFloat(y);
        buffer.putInt(stroke);
        buffer.putInt(color);
        buffer.put(booleanToByte(follower));

        return buffer.array();
    }

    public static Point getByBytes(byte[] bytes) {
        return new Point(bytes);
    }

    @Override
    public String toString() {
        return "(" + x + "," + y + ")" + " stroke: " + stroke + " color: " + color + "follower: " + follower;
    }

    private byte booleanToByte (boolean bool) {
        if(bool)
            return 1;
        else
            return 0;
    }

    private boolean byteToBoolean (byte number) {
        return number > 0;
    }
}
