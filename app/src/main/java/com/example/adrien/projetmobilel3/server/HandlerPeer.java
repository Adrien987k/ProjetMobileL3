package com.example.adrien.projetmobilel3.server;

import android.content.Intent;
import android.os.AsyncTask;
import android.view.InputDevice;

import com.example.adrien.projetmobilel3.common.Point;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by MrkJudge on 24/02/2017.
 */

//TODO non terminé + discuter de l'AsyncTask ou Thread
public class HandlerPeer extends AsyncTask<Socket,ArrayList<Point>,String>{

    private ServerP2P server;
    private Socket s;
    private OutputStream os;

    private boolean stop = false;
    private int pointPacketLenght = Point.getByteLength();
    private final ArrayList<Point> points = new ArrayList<>();

    public HandlerPeer(ServerP2P server) {
        this.server = server;
        server.getHandlers().add(this);
    }

    public OutputStream getOutPutSteam() { return os; }
    public ArrayList<Point> getPoints() { return points; }
    public synchronized ArrayList<Point> gatherPoints() {
        ArrayList<Point> pointsGathered = new ArrayList<>(getPoints());
        getPoints().clear();
        return pointsGathered;
    }

    @Override
    protected String doInBackground(Socket... params) {
        //TODO non terminé
        this.s = params[0];

        try {
            os = s.getOutputStream();
            InputStream buffer = s.getInputStream();

            while(!stop) {
                buffer.read(new byte[pointPacketLenght], 0, pointPacketLenght);
                //TODO méthode de déserialization + envoie des données vers l'application

            }
            s.close();points.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }

        server.getHandlers().remove(this);
        return null;
    }

    @Override
    protected void onProgressUpdate(ArrayList<Point>... values) {
        super.onProgressUpdate(values);
        //TODO non commencé
    }



}
