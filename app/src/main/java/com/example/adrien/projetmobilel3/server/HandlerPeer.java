package com.example.adrien.projetmobilel3.server;

import android.os.AsyncTask;

import com.example.adrien.projetmobilel3.common.Point;

import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by MrkJudge on 24/02/2017.
 */

public class HandlerPeer extends AsyncTask<Socket,ArrayList<Point>,String>{
    //TODO non terminé
    private Socket s;

    @Override
    protected String doInBackground(Socket... params) {
        //TODO non terminé

        this.s = params[0];

        return null;
    }

    @Override
    protected void onProgressUpdate(ArrayList<Point>... values) {
        super.onProgressUpdate(values);
        //TODO non commencé
    }
}
