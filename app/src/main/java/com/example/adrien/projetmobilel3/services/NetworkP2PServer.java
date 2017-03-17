package com.example.adrien.projetmobilel3.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.example.adrien.projetmobilel3.server.ServerP2P;

import java.io.IOException;
import java.net.ServerSocket;

public class NetworkP2PServer extends Service {

    private MyBinder myBinder = new MyBinder();
    private boolean stop = false;
    private ServerSocket ss;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        try {
            ss = new ServerSocket(ServerP2P.DEFAULT_PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("ServerService created");
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            ss.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("ServerService destroyed");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }

    public ServerSocket getSs() {
        return ss;
    }

    public class MyBinder extends Binder {
        public NetworkP2PServer getService() {
            return NetworkP2PServer.this;
        }
    }
}


