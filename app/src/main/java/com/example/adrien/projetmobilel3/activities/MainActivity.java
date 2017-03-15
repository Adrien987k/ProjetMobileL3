package com.example.adrien.projetmobilel3.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.adrien.projetmobilel3.R;
import com.example.adrien.projetmobilel3.common.HardwareAddress;
import com.example.adrien.projetmobilel3.common.PointPacket;
import com.example.adrien.projetmobilel3.common.PointTransmission;
import com.example.adrien.projetmobilel3.draw.Draw;
import com.example.adrien.projetmobilel3.draw.Point;
import com.example.adrien.projetmobilel3.server.ServerP2P;

public class MainActivity extends Activity {

    private final IntentFilter intentFilter = new IntentFilter();
    private WifiP2pManager wifiP2pManager = null;
    private Channel channel = null;
    private Receiver receiver = null;
    private PointTransmission transmission;

    private boolean isWifiP2pEnabled = false;
    public final MainActivity mainActivity = this;
    public boolean connected = false;

    private Draw draw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadingDisplay(true);

        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.activity_main);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, null, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);




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

        wifiP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = wifiP2pManager.initialize(this, getMainLooper(), null);
        receiver = new Receiver(wifiP2pManager, channel, this);
        wifiP2pManager.discoverPeers(channel,receiver.discover);
    }

    //TODO sauvegarde des données
    @Override
    public void onResume(){
        super.onResume();
        receiver = new Receiver(wifiP2pManager, channel, this);
        registerReceiver(receiver, intentFilter);
       // wifiP2pManager.discoverPeers(channel,receiver.discoverThenConnect);
        wifiP2pManager.requestConnectionInfo(channel, new WifiP2pManager.ConnectionInfoListener() {
            @Override
            public void onConnectionInfoAvailable(WifiP2pInfo info) {
                if(info.groupFormed) {
                    if(info.isGroupOwner) {
                        ServerP2P server = new ServerP2P(MainActivity.this);
                        setTransmission(server.getSynchronizer());

                    } else {
                     //   transmission = new ClientPeer(MainActivity.this, info.groupOwnerAddress);
                    }
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
        connected = false;
    }

    public void loadingDisplay(boolean isLoading) {
        if(isLoading) {
            findViewById(R.id.loading).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.loading).setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(!data.getBooleanExtra("localMode",true)) {
            if(data.getBooleanExtra("serverMode",false)) {
                if(transmission == null) {
                    ServerP2P server = new ServerP2P(MainActivity.this);
                    setTransmission(server.getSynchronizer());
                }
            } else {
                receiver.connect(data.getStringExtra("deviceName"));
            }
        }
        connected = true;
    }

/* Fusion DrawActivity */

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.activity_main);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.draw, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
