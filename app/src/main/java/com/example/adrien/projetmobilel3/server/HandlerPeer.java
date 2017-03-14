package com.example.adrien.projetmobilel3.server;

import android.app.Activity;
import android.app.NotificationManager;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.NotificationCompat;
import android.view.MotionEvent;
import android.widget.Toast;

import com.example.adrien.projetmobilel3.MainActivity;
import com.example.adrien.projetmobilel3.R;
import com.example.adrien.projetmobilel3.common.PointPacket;
import com.example.adrien.projetmobilel3.draw.Point;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by MrkJudge on 24/02/2017.
 */

//TODO non terminé
public class HandlerPeer extends Thread {

    private ServerP2P server;
    private Socket socket;
    private OutputStream os;

    private boolean stop = false;

    private Path path = new Path();
    private Paint paint = new Paint();


    public HandlerPeer(ServerP2P server,Socket socket) {
        this.server = server;
        this.socket = socket;
        server.getHandlers().add(this);
        init();
    }

    private void init() {
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
    }

    public MainActivity getMainActivity() {
        return server.getMainActivity();
    }

    //TODO en fonction de celle du synchronize
    public synchronized void gatherPoints() {}

    @Override
    public void run() {
        super.run();
        System.out.println("Handler created");
        try {
            os = socket.getOutputStream();
            InputStream buffer = socket.getInputStream();
            byte[] bufferData;
            while (!stop) {
                bufferData = new byte[PointPacket.BYTES];
                buffer.read(bufferData);
                handleData(new PointPacket(bufferData));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            server.getHandlers().remove(this);
            System.out.println("Handler Closed");
        }

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

    public void setStop(boolean stop) {
        this.stop = stop;
    }
}
