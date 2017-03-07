package com.example.adrien.projetmobilel3.server;

import com.example.adrien.projetmobilel3.MainActivity;

import java.io.IOException;
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

    private boolean stop = false;
    private int port;

    private MainActivity mainActivity;

    public ServerP2P(MainActivity mainActivity, int port) {
        this.port = port;
        this.mainActivity = mainActivity;
        start();
    }

    public ServerP2P(MainActivity activity) {
        this.port = DEFAULT_PORT ;
        this.mainActivity = activity;
        start();
    }

    @Override
    public void run() {
        try {
            ServerSocket ss = new ServerSocket(port);
            ss.setSoTimeout(1000);

            while(!stop) {
                try {
                    Socket s = ss.accept();
                   new HandlerPeer(this,s).start();
                } catch (SocketTimeoutException e) { }
            }

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
}
