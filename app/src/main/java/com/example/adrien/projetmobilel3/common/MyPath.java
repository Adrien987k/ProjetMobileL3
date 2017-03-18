package com.example.adrien.projetmobilel3.common;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * The MyPath class stores a list of point drawn by multiple users.
 * With the appropriate algorithm, you can easily redraw the whole
 * draw without conflict.
 * This is usually used when the activity restarts or when a new
 * user has arrived.
 */

public class MyPath implements Parcelable{

    /**
     * The array list of all points.
     */
    private final ArrayList<PointPacket> pointPackets = new ArrayList<>();

    /**
     * Create an empty path.
     */
    public MyPath() {
        super();
    }

    /**
     * Create a path and fill it with points from the parcel.
     * @param parcel The parcel containing points.
     */
    protected MyPath(Parcel parcel) {
        parcel.readParcelableArray(PointPacket.class.getClassLoader());
    }

    public static final Creator<MyPath> CREATOR = new Creator<MyPath>() {
        @Override
        public MyPath createFromParcel(Parcel in) {
            return new MyPath(in);
        }

        @Override
        public MyPath[] newArray(int size) {
            return new MyPath[size];
        }
    };

    /**
     * Add a point to the list.
     * @param pointPacket The point to add.
     */
    public void addPointPacket(PointPacket pointPacket) {
        this.pointPackets.add(pointPacket);
    }

    public ArrayList<PointPacket> getPointPackets() {
        return pointPackets;
    }

    /**
     * Parcelable interface methods.
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Write this path to the specified parcel.
     * @param parcel The parcel to write each point into.
     */
    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeParcelableArray(pointPackets.toArray(new PointPacket[pointPackets.size()]),i);
    }
}
