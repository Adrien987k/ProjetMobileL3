package com.example.adrien.projetmobilel3.common;

import android.os.Parcel;
import android.os.Parcelable;

import java.nio.ByteBuffer;

/**
 * The PointPacket class provide a packet optimized to be sent through the network.
 * The packet contains a point, an action from the event and a hardware address.
 */
public class PointPacket implements Parcelable{

    /**
     * Length in byte of a PointPacket.
     */
    public static final int BYTES = Point.BYTES + (Integer.SIZE / Byte.SIZE) + HardwareAddress.BYTES;

    /**
     * The point.
     */
    private Point point;

    /**
     * The action from the event.
     * When the TouchEvent is caught, an action is given to determine
     * how the user touched the screen.
     * This action will be useful to re-draw the draw.
     */
    private int action;

    /**
     * The hardware address of the drawer.
     */
    private HardwareAddress hardwareAddress;

    /**
     * Create a point packet with the specified point and action.
     * @param point The point drawn.
     * @param action The action from the TouchEvent.
     */
    private PointPacket(Point point, int action) {
        this.point = point;
        this.action = action;
    }

    /**
     * Create a point packet with the specified point, action and hardware address.
     * @param point The point drawn.
     * @param action The action from the TouchEvent.
     * @param hardwareAddress The hardware address of the drawer.
     */
    public PointPacket(Point point, int action, HardwareAddress hardwareAddress) {
        this(point,action);
        this.hardwareAddress = hardwareAddress;
    }

    /**
     * Create a point packet from a byte array containing, in this specific order,
     * the point, the action and the hardware address.
     * @param bytes The byte array containing the point packet information.
     */
    public PointPacket(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);

        this.point = new Point(buffer.getFloat(),buffer.getFloat(),buffer.getInt(),buffer.getInt());
        this.action = buffer.getInt();
        this.hardwareAddress = HardwareAddress.parseHardwareAddress(buffer);
    }


    /**
     * Create a point pcket from a parcel.
     * @param parcel The parcel containing the point packet.
     */
    protected PointPacket(Parcel parcel) {
        point = parcel.readParcelable(Point.class.getClassLoader());
        action = parcel.readInt();
    }

    public static final Creator<PointPacket> CREATOR = new Creator<PointPacket>() {
        @Override
        public PointPacket createFromParcel(Parcel in) {
            return new PointPacket(in);
        }

        @Override
        public PointPacket[] newArray(int size) {
            return new PointPacket[size];
        }
    };


    /**
     * Return a byte array containing all information about the point packet.
     * This format respect the specified one by the constructor.
     * @return The byte array containing information about the point packet.
     */
    public byte[] getBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(BYTES);

        buffer.put(point.getBytes());
        buffer.putInt(action);
        buffer.put(hardwareAddress.getBytes());

        return buffer.array();
    }

    public Point getPoint() {
        return point;
    }
    public int getAction() {
        return action;
    }
    public HardwareAddress getHardwareAddress() {
        return hardwareAddress;
    }


    /**
     * Return a string of all information about the point packet.
     * @return The string containing information about the point packet.
     */
    @Override
    public String toString() {
        return "" + point + " action: " + action + " MAC address: " + hardwareAddress;
    }

    /**
     * Parcelable interface methods.
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Write this point packet to the specified parcel.
     * @param parcel The parcel to write the point packet into.
     */
    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeParcelable(point, i);
        parcel.writeInt(action);
        parcel.writeByteArray(hardwareAddress.getBytes());
    }
}
