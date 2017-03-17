package com.example.adrien.projetmobilel3.common;

import android.os.Parcel;
import android.os.Parcelable;

import com.example.adrien.projetmobilel3.draw.Point;

import java.util.ArrayList;

/**
 * Created by MrkJudge on 16/03/2017.
 */

public class MyPath implements Parcelable{

    private final ArrayList<PointPacket> pointPackets = new ArrayList<>();

    public MyPath() {

    }

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

    public void addPointPacket(PointPacket pointPacket) {
        this.pointPackets.add(pointPacket);
    }

    public ArrayList<PointPacket> getPointPackets() {
        return pointPackets;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeParcelableArray(pointPackets.toArray(new PointPacket[pointPackets.size()]),i);
    }
}
