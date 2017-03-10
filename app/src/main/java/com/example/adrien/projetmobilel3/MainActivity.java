package com.example.adrien.projetmobilel3;

import android.app.NotificationManager;
import android.content.Context;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.support.v7.app.NotificationCompat;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.adrien.projetmobilel3.client.ClientPeer;
import com.example.adrien.projetmobilel3.common.PointTransmission;
import com.example.adrien.projetmobilel3.draw.Draw;
import com.example.adrien.projetmobilel3.draw.DrawSurface;
import com.example.adrien.projetmobilel3.draw.Point;
import com.example.adrien.projetmobilel3.server.PointSynchronizer;
import com.example.adrien.projetmobilel3.server.ServerP2P;

//TODO L'application plante quand l'un se déconnecte

public class MainActivity extends AppCompatActivity {

    private final IntentFilter intentFilter = new IntentFilter();
    private WifiP2pManager wifiP2pManager = null;
    private Channel channel = null;
    private Receiver receiver = null;
    private PointTransmission transmission;

    private boolean isWifiP2pEnabled = false;
    public final MainActivity mainActivity = this;

    private Draw draw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        wifiP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = wifiP2pManager.initialize(this, getMainLooper(), null);
        receiver = new Receiver(wifiP2pManager, channel, this);



        wifiP2pManager.discoverPeers(channel,receiver.discover);

/*
        //TODO: boutons à enlever
        // mais pratique pour tester rapidement
        Button discoverButton = (Button)findViewById(R.id.discover_button);
        discoverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wifiP2pManager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {}

                    @Override
                    public void onFailure(int reason) {
                        Toast.makeText(MainActivity.this, "Discover failed.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        Button connectButton = (Button)findViewById(R.id.connect_button);
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                receiver.connect();
            }
        });
*/
        draw = (Draw) findViewById(R.id.draw);
        draw.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                boolean follower = false;
                if(event.getAction() == MotionEvent.ACTION_MOVE)
                    follower = true;
                ((Draw) v).addPoint(new Point(event.getX(),event.getY(),20, Color.BLUE,follower));
                if(transmission != null)
                    transmission.addPoint(new Point(event.getX(),event.getY(),20, Color.RED,follower));
                return true;
            }
        });


/*
        //TODO à enlever si classe non corrigée
        DrawSurface drawSurface = (DrawSurface) findViewById(R.id.drawSurface);
        drawSurface.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                ((DrawSurface) v).addPoint(new Point(event.getX(),event.getY(),20, Color.BLUE));
                return true;
            }
        });*/

        Button refreshButton = (Button) findViewById(R.id.refresh_button);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.this.getDraw().clear();
            }
        });
    }

    //TODO sauvegarde des données
    @Override
    public void onResume(){
        super.onResume();
        receiver = new Receiver(wifiP2pManager, channel, this);
        registerReceiver(receiver, intentFilter);
        wifiP2pManager.discoverPeers(channel,receiver.discoverThenConnect);
        wifiP2pManager.requestConnectionInfo(channel, new WifiP2pManager.ConnectionInfoListener() {
            @Override
            public void onConnectionInfoAvailable(WifiP2pInfo info) {
                if(info.groupFormed) {
                    if(info.isGroupOwner) {
                        ServerP2P server = new ServerP2P(MainActivity.this);
                        setTransmission(server.getSynchronizer());
                        setTransmission(server.getSynchronizer());
                    } else {
                     //   transmission = new ClientPeer(MainActivity.this, info.groupOwnerAddress);
                    }
                } else {
                    Toast.makeText(mainActivity, "reconnexion failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onPause(){
        super.onPause();
        setStop(true);
        unregisterReceiver(receiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        setStop(true);
    }

    public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled){
        this.isWifiP2pEnabled = isWifiP2pEnabled;
    }

    public void setTransmission(PointTransmission transmission) {
        this.transmission = transmission;
    }

    public Channel getChannel() {
        return channel;
    }

    public Draw getDraw() {
        return draw;
    }

    public void onClickPeerDiscovered(View v) {
        receiver.connect(((TextView) v).getText().toString());
    }

    private void setStop(boolean stop) {
        if(transmission != null)
            transmission.setStop(stop);
    }
}
