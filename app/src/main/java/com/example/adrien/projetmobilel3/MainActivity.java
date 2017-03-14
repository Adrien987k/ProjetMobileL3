package com.example.adrien.projetmobilel3;

import android.content.Context;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.adrien.projetmobilel3.common.HardwareAddress;
import com.example.adrien.projetmobilel3.common.PointPacket;
import com.example.adrien.projetmobilel3.common.PointTransmission;
import com.example.adrien.projetmobilel3.draw.Draw;
import com.example.adrien.projetmobilel3.draw.Point;
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

        draw = (Draw) findViewById(R.id.draw);
        draw.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                ((Draw) v).addEvent(event);
                if(transmission != null)
                    transmission.addPointPacket(new PointPacket(new Point(event.getX(),event.getY(),20, Color.RED),event.getAction(),receiver.getHardwareAddress()));
                return true;
            }
        });

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
    public PointTransmission getTransmission() {
        return transmission;
    }
    public HardwareAddress getHardwareAddress() {
        return receiver.getHardwareAddress();
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
