package com.example.adrien.projetmobilel3.server;

import android.app.Activity;
import android.app.NotificationManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.NotificationCompat;
import android.widget.Toast;

import com.example.adrien.projetmobilel3.R;
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
    private boolean stop = false;
    private final ArrayList<Point> points = new ArrayList<>();

    public HandlerPeer(ServerP2P server,Socket socket) {
        this.server = server;
        this.socket = socket;
        server.getHandlers().add(this);
    }

    public ArrayList<Point> getPoints() { return points; }
    public synchronized ArrayList<Point> gatherPoints() {
        ArrayList<Point> pointsGathered = new ArrayList<>(getPoints());
        getPoints().clear();
        return pointsGathered;
    }

    @Override
    public void run() {
        super.run();
        System.out.println("Handler created");
        try {
            InputStream buffer = socket.getInputStream();
            byte[] bufferData;
            while (!stop) {
                bufferData = new byte[Point.getByteLength()];
                buffer.read(bufferData);
                server.getMainActivity().getDraw().addPoint(new Point(bufferData),false);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            server.getHandlers().remove(this);
            System.out.println("Handler Closed");
        }

    }

    public synchronized void sendPoints(ArrayList<Point> points) {
        try {
            for (Point point : points) {
                socket.getOutputStream().write(point.getBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setStop(boolean stop) {
        this.stop = stop;
    }
}
