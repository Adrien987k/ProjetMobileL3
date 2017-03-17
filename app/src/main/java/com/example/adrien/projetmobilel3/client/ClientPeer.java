package com.example.adrien.projetmobilel3.client;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Path;
import android.net.ConnectivityManager;
import android.view.MotionEvent;

import com.example.adrien.projetmobilel3.activities.MainActivity;
import com.example.adrien.projetmobilel3.activities.Receiver;
import com.example.adrien.projetmobilel3.common.DrawTools;
import com.example.adrien.projetmobilel3.common.HardwareAddress;
import com.example.adrien.projetmobilel3.common.Message;
import com.example.adrien.projetmobilel3.common.PointPacket;
import com.example.adrien.projetmobilel3.common.PointTransmission;
import com.example.adrien.projetmobilel3.draw.Draw;
import com.example.adrien.projetmobilel3.server.ServerP2P;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.TreeMap;

/**
 * Created by MrkJudge on 08/03/2017.
 */

public class ClientPeer extends Thread implements PointTransmission {

    private Socket socket;
    private OutputStream os;

    private MainActivity mainActivity;
    private HardwareAddress hardwareAddress;
    private InetAddress serverAddress;

    private boolean stop = false;
    private boolean connexionEstablished = false;


    //TODO not working
    private boolean connexionAttempt = false;
    public ClientPeer(MainActivity mainActivity, InetAddress serverAddress, HardwareAddress hardwareAddress) {
        this.mainActivity = mainActivity;
        this.serverAddress = serverAddress;
        this.hardwareAddress = hardwareAddress;
        start();
    }

    @Override
    public void run() {
        super.run();
        ConnectivityManager cm = (ConnectivityManager) (mainActivity.getSystemService(Context.CONNECTIVITY_SERVICE));
        if(cm != null
                && cm.getActiveNetworkInfo().isConnected())  {

            socket = null;
            try {
                socket = new Socket(serverAddress, ServerP2P.DEFAULT_PORT);
                connexionEstablished = true;
                this.os = socket.getOutputStream();
                os.write(new Message(hardwareAddress).getBytes());
                System.out.println("Socket created, client side");
                connexionAttempt = true;
                //System.out.println(hardwareAddress);
            } catch (IOException e) {
                connexionAttempt = true;
                setStop(true);
                e.printStackTrace();
                return;
            }

        try {
            InputStream buffer = socket.getInputStream();
            byte[] bufferData;
            while(!stop) {
                bufferData = new byte[PointPacket.BYTES];
                buffer.read(bufferData);
                handleData(new PointPacket(bufferData));
            }
        } catch (SocketException e) {
            e.printStackTrace();
            setStop(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
            System.out.println("Socket Closed, client side");
        }
        destroy();
    }


    private synchronized void handleData(PointPacket pointPacket) {
        HardwareAddress hardwareAddressReceived = pointPacket.getHardwareAddress();
        getMainActivity().getDraw().getPoints().add(pointPacket);
        if(!hardwareAddressReceived.equals(hardwareAddress)) {
            if (getUsers().containsKey(hardwareAddressReceived)) {
                getDraw().drawPointPacket(getUsers().get(hardwareAddressReceived), pointPacket);
            } else {
                getUsers().put(new HardwareAddress(hardwareAddressReceived.getBytes()), new DrawTools());
                getDraw().drawPointPacket(getUsers().get(hardwareAddressReceived), pointPacket);
            }
        }
    }
/*
    private synchronized void drawPointPacket(DrawTools drawTools, PointPacket pointPacket) {
        float x = pointPacket.getPoint().getX();
        float y = pointPacket.getPoint().getY();
        Path path = drawTools.getPath();
        Paint paint = drawTools.getPaint();

        switch (pointPacket.getAction()) {
            case MotionEvent.ACTION_DOWN:
                paint.setStrokeWidth(pointPacket.getPoint().getStroke());
                paint.setColor(pointPacket.getPoint().getColor());
                path.moveTo(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                myLineTo(x, y, drawTools);
                break;
            case MotionEvent.ACTION_UP:
                myLineTo(x, y, drawTools);
                path.reset();
                break;
        }
    }

    private synchronized void myLineTo(float x, float y, DrawTools drawTools) {
        Path path = drawTools.getPath();
        Paint paint = drawTools.getPaint();

        path.lineTo(x, y);
        getDraw().getMyCanvas().drawPath(path, paint);
        getDraw().postInvalidate();
        path.reset();
        path.moveTo(x,y);
    }
*/
    private MainActivity getMainActivity() {
        return mainActivity;
    }
    private Draw getDraw() {
        return getMainActivity().getDraw();
    }
    private HardwareAddress getHardwareAddress() {
        return getMainActivity().getHardwareAddress();
    }
    private TreeMap<HardwareAddress,DrawTools> getUsers() {
        return getMainActivity().getUsers();
    }

    @Override
    public void setStop(boolean stop) {
        this.stop = stop;
        connexionEstablished = false;
    }

    @Override
    public synchronized void addPointPacket(PointPacket pointPacket) {
        try {
            os.write(pointPacket.getBytes());
        } catch (SocketException e) {
            e.printStackTrace();
            setStop(true);
        } catch (IOException e) {
            e.printStackTrace();
            setStop(true);
        } catch (NullPointerException e) {
            e.printStackTrace();
            setStop(true);
        }
    }

    public boolean connexionEstablished() {
        while (!connexionAttempt) {}
        return connexionEstablished;
    }


}

