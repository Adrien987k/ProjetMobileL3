package com.example.adrien.projetmobilel3.server;

import android.os.AsyncTask;

import com.example.adrien.projetmobilel3.common.Point;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

/**
 * Created by MrkJudge on 24/02/2017.
 */

public class ServerP2P extends Thread {

    private final ArrayList<AsyncTask> handlers = new ArrayList<>();

    private boolean stop = false;
    private int port;

    public ServerP2P(int port) {
        this.port = port;
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
                   new HandlerPeer(this).execute(s);
                } catch (SocketTimeoutException e) { }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public ArrayList<AsyncTask> getHandlers() {
        return handlers;
    }
}
