package com.example.adrien.projetmobilel3.client;

import android.content.Context;
import android.drm.DrmStore;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.view.MotionEvent;

import com.example.adrien.projetmobilel3.MainActivity;
import com.example.adrien.projetmobilel3.common.PointPacket;
import com.example.adrien.projetmobilel3.common.PointTransmission;
import com.example.adrien.projetmobilel3.draw.Point;
import com.example.adrien.projetmobilel3.server.ServerP2P;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ThreadFactory;

/**
 * Created by MrkJudge on 08/03/2017.
 */

public class ClientPeer extends Thread implements PointTransmission {

    private Socket socket;
    private OutputStream os;

    private MainActivity mainActivity;
    private InetAddress serverAddress;

    private Path path = new Path();
    private Paint paint = new Paint();

    private boolean stop = false;

    public ClientPeer(MainActivity mainActivity, InetAddress serverAddress) {
        this.mainActivity = mainActivity;
        this.serverAddress = serverAddress;
        init();
        start();
    }

    private void init() {
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
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
                os = socket.getOutputStream();
                System.out.println("Socket created, client side");
                this.os = socket.getOutputStream();
            } catch (IOException e) {
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
    }


    private void handleData(PointPacket pointPacket) {
        float x = pointPacket.getPoint().getX();
        float y = pointPacket.getPoint().getY();

        switch (pointPacket.getAction()) {
            case MotionEvent.ACTION_DOWN:
                paint.setStrokeWidth(pointPacket.getPoint().getStroke());
                paint.setColor(pointPacket.getPoint().getColor());
                path.moveTo(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                myLineTo(x, y);
                break;
            case MotionEvent.ACTION_UP:
                myLineTo(x, y);
                path.reset();
                break;
        }
    }

    private void myLineTo(float x, float y) {
        path.lineTo(x, y);
        path.moveTo(x,y);
        getMainActivity().getDraw().getMyCanvas().drawPath(path,paint);
        getMainActivity().getDraw().postInvalidate();
    }

    private MainActivity getMainActivity() {
        return mainActivity;
    }

    @Override
    public void setStop(boolean stop) {
        this.stop = stop;
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
        }
    }


}

