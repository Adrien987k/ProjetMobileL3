package com.example.adrien.projetmobilel3.server;

import android.os.AsyncTask;

import com.example.adrien.projetmobilel3.common.HardwareAddress;
import com.example.adrien.projetmobilel3.common.Point;
import com.example.adrien.projetmobilel3.common.PointPacket;
import com.example.adrien.projetmobilel3.common.PointTransmission;
import com.example.adrien.projetmobilel3.draw.Draw;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

/**
 * The PointSynchronize class will gather points from each user
 * and send them to each others.
 * The server will add directly his point to the synchronizer while
 * the synchronizer will gather points from handlers.
 */

public class PointSynchronizer extends AsyncTask<HashMap<HardwareAddress,HandlerPeer>,ArrayList<Point>,Boolean> implements PointTransmission{

    /**
     * The frequency at which the synchronizer will gather and send points.
     */
    public static final int REFRESH_RATE = 100;

    /**
     * All new points from every users.
     */
    private final ArrayList<PointPacket> pointPackets = new ArrayList<>();

    /**
     * All points drawn from the beginning.
     * They are sent after the init message for any new user.
     */
    private final ArrayList<PointPacket> pointPacketsFromStart = new ArrayList<>();

    /**
     * The hash map containing handlers by their client's hardware address.
     */
    private HashMap<HardwareAddress,HandlerPeer> handlers;

    /**
     * The server.
     */
    private ServerP2P server;

    /**
     * Indicate if the synchronizer must stop.
     */
    private boolean stop = false;

    /**
     * Create a synchronizer boud to the specified server.
     * @param server
     */
    public PointSynchronizer(ServerP2P server) {
        this.server = server;
    }

    /**
     * Link to the draw of the server user.
     */
    public Draw getDraw() {
        return server.getDrawActivity().getDraw();
    }

    @Override
    protected Boolean doInBackground(HashMap<HardwareAddress,HandlerPeer>... params) {
        this.handlers = params[0];

        while(!stop) {
            try {
                Thread.sleep(REFRESH_RATE);
                gatherPoints();
                    HashMap<HardwareAddress,HandlerPeer> knownHandlers = new HashMap<>(handlers);
                ArrayList<PointPacket> knownPointPackets = new ArrayList<>(getPointPackets());
                getPointPackets().clear();
                for (HandlerPeer handler : knownHandlers.values()) {
                        handler.sendPointPackets(knownPointPackets);
                    }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return true;
    }

    /**
     * Gather all points from each handler.
     */
    private synchronized void gatherPoints(){
        for(HandlerPeer handler: handlers.values()) {
            getPointPackets().addAll(handler.gatherPoints());
            pointPacketsFromStart.addAll(pointPackets);
        }
    }

    /**
     * @param pointPacket The point packet to send.
     */
    @Override
    public synchronized void addPointPacket(PointPacket pointPacket) {
        getPointPackets().add(pointPacket);
        pointPacketsFromStart.add(pointPacket);
    }

    public ArrayList<PointPacket> getPointPackets() {
        return pointPackets;
    }

    /**
     * Return all points drawn from the beginning.
     * @return The array list of all points.
     */
    public ArrayList<PointPacket> getPointPacketsFromStart() {
        return pointPacketsFromStart;
    }

    /**
     * Set the synchronizer state.
     * @param stop True if the synchronizer must stop.
     */
    @Override
    public void setStop(boolean stop) {
        this.stop = stop;
    }
}
