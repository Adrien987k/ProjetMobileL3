package com.example.adrien.projetmobilel3.server;

import android.graphics.Paint;
import android.graphics.Path;
import android.view.MotionEvent;

import com.example.adrien.projetmobilel3.activities.MainActivity;
import com.example.adrien.projetmobilel3.common.DrawTools;
import com.example.adrien.projetmobilel3.common.HardwareAddress;
import com.example.adrien.projetmobilel3.common.Message;
import com.example.adrien.projetmobilel3.common.PointPacket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

/**
 * Created by MrkJudge on 24/02/2017.
 */

//TODO non terminé
public class HandlerPeer extends Thread {

    private ServerP2P server;
    private Socket socket;
    private OutputStream os;

    private boolean stop = false;

    private Path localPath = new Path();
    private Paint localPaint = new Paint();

    private final ArrayList<PointPacket> pointPackets = new ArrayList<>();


    public HandlerPeer(ServerP2P server,Socket socket) {
        this.server = server;
        this.socket = socket;
        init();
    }

    private void init() {
        localPaint.setAntiAlias(true);
        localPaint.setStyle(Paint.Style.STROKE);
        localPaint.setStrokeJoin(Paint.Join.ROUND);
        localPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    public MainActivity getMainActivity() {
        return server.getMainActivity();
    }
    public DrawTools getDrawTools() {
        return new DrawTools(localPath, localPaint);
    }
    public ArrayList<PointPacket> getPointPackets() {
        return pointPackets;
    }

    //TODO en fonction de celle du synchronize
    public synchronized ArrayList<PointPacket> gatherPoints() {
        ArrayList<PointPacket> knownPointPackets = new ArrayList<>(pointPackets);
        pointPackets.clear();
        return knownPointPackets;
    }

    @Override
    public void run() {
        super.run();
        System.out.println("Handler created");
        try {
            os = socket.getOutputStream();
            InputStream buffer = socket.getInputStream();
            byte[] bufferData;

            bufferData = new byte[HardwareAddress.BYTES];

            buffer.read(bufferData);
            initMessage(new Message(bufferData));

            while (!stop) {
                bufferData = new byte[PointPacket.BYTES];
                buffer.read(bufferData);
                handleData(new PointPacket(bufferData));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            server.getHandlers().remove(this);
            System.out.println("Handler unregistered");
            System.out.println("Handler Closed");
        }

    }

    public void initMessage(Message message) {
        server.getHandlers().put(message.getHardwareAddress(),this);
        System.out.println("Handler registered");
        //System.out.println(message.getHardwareAddress());

    }

    public synchronized void sendPointPackets(ArrayList<PointPacket> pointPackets) {
        try {
            for(PointPacket pointPacket: pointPackets)
                os.write(pointPacket.getBytes());
        } catch (SocketException e) {
            e.printStackTrace();
            setStop(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleData(PointPacket pointPacket) {

        pointPackets.add(pointPacket);

        float x = pointPacket.getPoint().getX();
        float y = pointPacket.getPoint().getY();

        switch (pointPacket.getAction()) {
            case MotionEvent.ACTION_DOWN:
                localPaint.setStrokeWidth(pointPacket.getPoint().getStroke());
                localPaint.setColor(pointPacket.getPoint().getColor());
                localPath.moveTo(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                myLineTo(x, y);
                break;
            case MotionEvent.ACTION_UP:
                myLineTo(x, y);
                localPath.reset();
                break;
        }
    }

    private synchronized void myLineTo(float x, float y) {
        localPath.lineTo(x, y);
        getMainActivity().getDraw().getMyCanvas().drawPath(localPath, localPaint);
        getMainActivity().getDraw().postInvalidate();
        localPath.reset();
        localPath.moveTo(x,y);
    }

    public void setStop(boolean stop) {
        this.stop = stop;
    }
}
