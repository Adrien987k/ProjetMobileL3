package com.example.adrien.projetmobilel3.common;

import com.example.adrien.projetmobilel3.draw.Point;

import java.nio.ByteBuffer;

/**
 * Created by MrkJudge on 13/03/2017.
 */

public class PointPacket {

    public static final int BYTES = Point.BYTES + (Integer.SIZE / Byte.SIZE) + HardwareAddress.BYTES;

    private Point point;
    private int action;
    private HardwareAddress hardwareAddress;

    private PointPacket(Point point, int action) {
        this.point = point;
        this.action = action;
    }
    public PointPacket(Point point, int action, HardwareAddress hardwareAddress) {
        this(point,action);
        this.hardwareAddress = hardwareAddress;
    }

    public PointPacket(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);

        this.point = new Point(buffer.getFloat(),buffer.getFloat(),buffer.getInt(),buffer.getInt());
        this.action = buffer.getInt();
        this.hardwareAddress = HardwareAddress.parseHardwareAddress(buffer);
    }

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




}
