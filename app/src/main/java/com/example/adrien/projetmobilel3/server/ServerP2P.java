package com.example.adrien.projetmobilel3.server;

import com.example.adrien.projetmobilel3.MainActivity;
import com.example.adrien.projetmobilel3.common.PointTransmission;
import com.example.adrien.projetmobilel3.draw.Point;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

/**
 * Created by MrkJudge on 24/02/2017.
 */

public class ServerP2P extends Thread {

    public static final int DEFAULT_PORT = 47856;

    private final ArrayList<HandlerPeer> handlers = new ArrayList<>();

    private PointSynchronizer synchronizer;

    private boolean stop = false;
    private int port;

    private MainActivity mainActivity;

    public ServerP2P(MainActivity mainActivity, int port) {
        this.port = port;
        this.mainActivity = mainActivity;
        this.synchronizer = new PointSynchronizer(this);

        synchronizer.execute(handlers);
        start();
    }

    public ServerP2P(MainActivity mainActivity) {
        this(mainActivity,DEFAULT_PORT);
    }

    @Override
    public void run() {
        try (ServerSocket ss = new ServerSocket(port);){
            System.out.println("Server Created");
            ss.setSoTimeout(1000);
            while(!stop) {
                try {
                    Socket s = ss.accept();
                    new HandlerPeer(this,s).start();
                } catch (SocketTimeoutException e) { }
            }
            System.out.println("Server Closed");
            ss.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<HandlerPeer> getHandlers() {
        return handlers;
    }

    public MainActivity getMainActivity() {
        return mainActivity;
    }

    public PointSynchronizer getSynchronizer() {
        return synchronizer;
    }

    public void setStop(boolean stop) {
        this.stop = stop;
        for(HandlerPeer handler: handlers)
            handler.setStop(stop);
    }

}
