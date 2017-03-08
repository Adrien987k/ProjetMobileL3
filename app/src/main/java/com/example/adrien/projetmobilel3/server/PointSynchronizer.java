package com.example.adrien.projetmobilel3.server;

import android.os.AsyncTask;

import com.example.adrien.projetmobilel3.common.PointTransmission;
import com.example.adrien.projetmobilel3.draw.Point;

import java.util.ArrayList;

/**
 * Created by MrkJudge on 25/02/2017.
 */

//TODO non terminé + discuter de l'AsyncTask ou Thread
public class PointSynchronizer extends AsyncTask<ArrayList<HandlerPeer>,ArrayList<Point>,String> implements PointTransmission{

    private ArrayList<HandlerPeer> handlers;
    private ArrayList<Point> points;

    public static final int REFRESH_RATE = 100;

    private boolean stop = false;

    @Override
    protected String doInBackground(ArrayList<HandlerPeer>... params) {
        //TODO non terminé
        this.handlers = params[0];
        this.points = new ArrayList<>();

        while(!stop) {
            try {
                Thread.sleep(100);
                gatherPoints();

                //TODO envoie par intent (ou autre) des points
                //TODO choisir si on garde tous les points ou non

            } catch (InterruptedException e) {}
        }

        return null;
    }

    @Override
    protected void onProgressUpdate(ArrayList<Point>... values) {
        super.onProgressUpdate(values);
        //TODO non commencé
    }

    private synchronized void gatherPoints() {
        for(HandlerPeer handler: handlers) {
            getPoints().addAll(handler.gatherPoints());
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