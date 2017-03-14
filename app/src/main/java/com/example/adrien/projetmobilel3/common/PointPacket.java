package com.example.adrien.projetmobilel3.common;

import com.example.adrien.projetmobilel3.draw.Point;

import java.nio.ByteBuffer;

/**
 * Created by MrkJudge on 13/03/2017.
 */

public class PointPacket {

    public static final int BYTES = Point.BYTES + (Integer.SIZE / Byte.SIZE);

    private Point point;
    private int action;

    public PointPacket(Point point, int action) {
        this.point = point;
        this.action = action;
    }

    public PointPacket(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);

        this.point = new Point(buffer.getFloat(),buffer.getFloat(),buffer.getInt(),buffer.getInt());
        this.action = buffer.getInt();
    }

    public byte[] getBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(BYTES);

        buffer.put(point.getBytes());
        buffer.putInt(action);

        return buffer.array();
    }

    public Point getPoint() {
        return point;
    }

    public int getAction() {
        return action;
    }




}
