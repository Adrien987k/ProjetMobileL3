package com.example.adrien.projetmobilel3.activities;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.example.adrien.projetmobilel3.server.ServerP2P;

public class Server extends Service {

    public Server(MainActivity mainActivity) {
        new ServerP2P(mainActivity);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
