package com.example.adrien.projetmobilel3.server;

import android.os.AsyncTask;

import com.example.adrien.projetmobilel3.common.HardwareAddress;
import com.example.adrien.projetmobilel3.common.PointPacket;
import com.example.adrien.projetmobilel3.common.PointTransmission;
import com.example.adrien.projetmobilel3.draw.Draw;
import com.example.adrien.projetmobilel3.draw.Point;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by MrkJudge on 25/02/2017.
 */

//TODO non terminé
public class PointSynchronizer extends AsyncTask<HashMap<HardwareAddress,HandlerPeer>,ArrayList<Point>,Boolean> implements PointTransmission{

    public static final int REFRESH_RATE = 100;

    private final ArrayList<PointPacket> pointPackets = new ArrayList<>();
    private HashMap<HardwareAddress,HandlerPeer> handlers;

    private ServerP2P server;
    private boolean stop = false;

    public PointSynchronizer(ServerP2P server) {
        this.server = server;
    }

    public Draw getDraw() {
        return server.getDrawActivity().getDraw();
    }

    @Override
    protected Boolean doInBackground(HashMap<HardwareAddress,HandlerPeer>... params) {
        //TODO non terminé
        this.handlers = params[0];

        while(!stop) {
            try {
                Thread.sleep(REFRESH_RATE);
                gatherPoints();
                    HashMap<HardwareAddress,HandlerPeer> knownHandlers = new HashMap<>(handlers);
                    ArrayList<PointPacket> knownPointPackets = new ArrayList<>(pointPackets);
                    pointPackets.clear();
                    for (HandlerPeer handler : knownHandlers.values()) {
                        handler.sendPointPackets(knownPointPackets);
                    }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return true;
    }

    private synchronized void gatherPoints(){
        for(HandlerPeer handler: handlers.values()) {
            pointPackets.addAll(handler.gatherPoints());
        }
    }

    @Override
    public synchronized void addPointPacket(PointPacket pointPacket) {
        pointPackets.add(pointPacket);
    }

    @Override
    public void setStop(boolean stop) {
        this.stop = stop;
    }
}
