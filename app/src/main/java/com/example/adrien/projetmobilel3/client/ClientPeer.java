package com.example.adrien.projetmobilel3.client;

import android.content.Context;
import android.drm.DrmStore;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.AsyncTask;

import com.example.adrien.projetmobilel3.MainActivity;
import com.example.adrien.projetmobilel3.common.PointTransmission;
import com.example.adrien.projetmobilel3.draw.Point;
import com.example.adrien.projetmobilel3.server.ServerP2P;

import java.io.IOException;
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

    private MainActivity mainActivity;
    private InetAddress serverAddress;

    private boolean stop = false;
    private final ArrayList<Point> points = new ArrayList<>();

    public ClientPeer(MainActivity mainActivity, InetAddress serverAddress) {
        this.mainActivity = mainActivity;
        this.serverAddress = serverAddress;
        start();
    }

    @Override
    public void run() {
        super.run();
        ConnectivityManager cm = (ConnectivityManager) (mainActivity.getSystemService(Context.CONNECTIVITY_SERVICE));
        if(cm != null
                && cm.getActiveNetworkInfo().isConnected())  {

            Socket socket = null;
            try {
                socket = new Socket(serverAddress, ServerP2P.DEFAULT_PORT);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            new AsyncTask<Socket,Object,Object>() {

                @Override
                protected Object doInBackground(Socket... params) {

                    Socket socket = params[0];
                    while(true) {
                        try {
                            if (getPoints().size() > 0) {

                                ArrayList<Point> knownPoints = new ArrayList<>(getPoints());
                                for (Point p : knownPoints) {
                                    socket.getOutputStream().write(p.getBytes());
                                }


                                getPoints().clear();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }.execute(new Socket[] {socket});

            while(!stop) {
                try {

                    byte[] buffer = new byte[Point.getByteLength()];
                    socket.getInputStream().read(buffer);
                    mainActivity.getDraw().addPoint(new Point(buffer));

                } catch (SocketException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                } finally {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        }
    }

    private ArrayList<Point> getPoints() {
        return points;
    }

    @Override
    public synchronized void addPoint(Point point) {
        getPoints().add(point);
    }

    @Override
    public synchronized void addAllPoints(ArrayList<Point> points) {
        getPoints().addAll(points);
    }
}
