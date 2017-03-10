package com.example.adrien.projetmobilel3.server;

import android.os.AsyncTask;

import com.example.adrien.projetmobilel3.common.PointTransmission;
import com.example.adrien.projetmobilel3.draw.Point;

import java.lang.reflect.Array;
import java.net.SocketException;
import java.util.ArrayList;

/**
 * Created by MrkJudge on 25/02/2017.
 */

//TODO non terminé
public class PointSynchronizer extends AsyncTask<ArrayList<HandlerPeer>,ArrayList<Point>,Boolean> implements PointTransmission{

    private ArrayList<HandlerPeer> handlers;
    private final ArrayList<Point> points = new ArrayList<>();
    private ServerP2P server;
    public static final int REFRESH_RATE = 100;

    private boolean stop = false;

    public PointSynchronizer(ServerP2P server) {
        this.server = server;
    }

    @Override
    protected Boolean doInBackground(ArrayList<HandlerPeer>... params) {
        //TODO non terminé
        this.handlers = params[0];

        while(!stop) {
            try {
                Thread.sleep(100);
                gatherPoints();
                synchronized (points) {
                    ArrayList<Point> knownPoints = new ArrayList<>(points);
                    points.clear();
                    ArrayList<HandlerPeer> knownHandlers = new ArrayList<>(handlers);
                    for (HandlerPeer handler : knownHandlers) {
                        handler.sendPoints(knownPoints);
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return true;
    }

    private synchronized void gatherPoints(){
        synchronized (handlers) {
            for (HandlerPeer handler : handlers) {
                getPoints().addAll(handler.gatherPoints());
            }
        }
    }

    @Override
    public void setStop(boolean stop) {
        this.stop = stop;
        server.setStop(stop);
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
