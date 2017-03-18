package com.example.adrien.projetmobilel3.common;

import android.os.Parcel;
import android.os.Parcelable;

import java.nio.ByteBuffer;

/**
 * The Point class represents a point with (x,y) coordinates,
 * with his stroke and his color.
 */
public class Point implements Parcelable {

    /**
     * Length in byte of a Point.
     */
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

    /**
     * Create a point with the specified coordinates, stroke and color.
     * @param x
     * @param y
     * @param stroke
     * @param color
     */
    public Point(float x, float y, int stroke, int color) {
        this.x = x;
        this.y = y;
        this.stroke = stroke;
        this.color = color;
    }

    /**
     * Create a point from a byte array.
     * @param bytes The byte array containing, in this specific order,
     *              coordinates x, y, stroke and color of the point.
     */
    public Point(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        this.x = buffer.getFloat();
        this.y = buffer.getFloat();
        this.stroke = buffer.getInt();
        this.color = buffer.getInt();
    }

    /**
     * Create a point from a parcel.
     * @param parcel The parcel containing, in this specific order,
     *              coordinates x, y, stroke and color of the point.
     */
    public Point(Parcel parcel) {
        this.x = parcel.readFloat();
        this.y = parcel.readFloat();
        this.stroke = parcel.readInt();
        this.color = parcel.readInt();
    }

    public float getX() { return x; }
    public float getY() { return y; }
    public int getStroke() { return stroke; }
    public int getColor() { return color; }

    /**
     * Parcelable interface methods.
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Write this point to the specified parcel.
     * @param dest The parcel to write the point into.
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(x);
        dest.writeFloat(y);
        dest.writeInt(stroke);
        dest.writeInt(color);
    }

    /**
     * Return a byte array with all information about the point.
     * This format respect the specified one by the constructor.
     * @return The byte array containing information about the point.
     */
    public byte[] getBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(BYTES);

        buffer.putFloat(x);
        buffer.putFloat(y);
        buffer.putInt(stroke);
        buffer.putInt(color);

        return buffer.array();
    }

    /**
     * Return a string of all information about the point.
     * @return The string containing information about the point.
     */
    @Override
    public String toString() {
        return "(" + x + "," + y + ")" + " stroke: " + stroke + " color: " + color;
    }

}
