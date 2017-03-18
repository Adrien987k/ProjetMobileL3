package com.example.adrien.projetmobilel3.server;

import com.example.adrien.projetmobilel3.activities.DrawActivity;
import com.example.adrien.projetmobilel3.common.HardwareAddress;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.HashMap;

/**
 * Created by MrkJudge on 24/02/2017.
 */

public class ServerP2P extends Thread {

    /**
     * The default port to bind the server.
     */
    public static final int DEFAULT_PORT = 47856;

    /**
     * The hash map where handlers are bind to the hardware address of their client.
     */
    private final HashMap<HardwareAddress,HandlerPeer> handlers = new HashMap<>();

    /**
     * The point synchronizer.
     * Check PointSynchronizer documentation for more.
     */
    private PointSynchronizer synchronizer;

    /**
     * Indicate if the server must stop.
     */
    private boolean stop = false;

    /**
     * The port to bind the server.
     */
    private int port;

    /**
     * The link to draw activity.
     */
    private DrawActivity drawActivity;

    /**
     * Create a server and bind it to the specified port.
     * @param port The port to bind the server.
     */
    public ServerP2P(DrawActivity drawActivity, int port) {
        this.port = port;
        this.drawActivity = drawActivity;
        this.synchronizer = new PointSynchronizer(this);

        synchronizer.execute(handlers);
        start();
    }

    /**
     * Create a sever and bint it to the default port.
     */
    public ServerP2P(DrawActivity drawActivity) {
        this(drawActivity,DEFAULT_PORT);
    }

    @Override
    public void run() {
        try (ServerSocket ss = new ServerSocket(port)){
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

    public HashMap<HardwareAddress,HandlerPeer> getHandlers() {
        return handlers;
    }

    public DrawActivity getDrawActivity() {
        return drawActivity;
    }

    public PointSynchronizer getSynchronizer() {
        return synchronizer;
    }

    public void setStop(boolean stop) {
        this.stop = stop;
        synchronizer.setStop(true);
        for(HandlerPeer handler: handlers.values())
            handler.setStop(stop);
    }

}
