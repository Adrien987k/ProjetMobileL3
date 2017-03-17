package com.example.adrien.projetmobilel3.activities;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.adrien.projetmobilel3.R;
import com.example.adrien.projetmobilel3.common.DrawTools;
import com.example.adrien.projetmobilel3.common.HardwareAddress;
import com.example.adrien.projetmobilel3.common.MyPath;
import com.example.adrien.projetmobilel3.common.P2PServerFragment;
import com.example.adrien.projetmobilel3.common.PointPacket;
import com.example.adrien.projetmobilel3.common.PointTransmission;
import com.example.adrien.projetmobilel3.draw.Draw;
import com.example.adrien.projetmobilel3.draw.Point;
import com.example.adrien.projetmobilel3.server.ServerP2P;
import com.example.adrien.projetmobilel3.services.NetworkP2PServer;

import java.util.ArrayList;
import java.util.TreeMap;

public class MainActivity extends Activity {

    public static final int LOCAL = 1;
    public static final int SERVER = 2;
    public static final int CLIENT = 3;
    public static final int NONE = 0;

    private final IntentFilter intentFilter = new IntentFilter();
    private WifiP2pManager wifiP2pManager = null;
    private Channel channel = null;
    private Receiver receiver = null;
    private PointTransmission transmission;

    public int connexionMode = NONE;
    private boolean isWifiP2pEnabled = false;
    public final MainActivity mainActivity = this;
    public boolean connected = false;
    private boolean drawable = false;
    private boolean startConnexionActivity = true;

    private Draw draw;

    private ArrayList<MyPath> paths = new ArrayList<>();
    private final TreeMap<HardwareAddress,DrawTools> users = new TreeMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadingDisplay(true);

        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.activity_main);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, null, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        draw = (Draw) findViewById(R.id.draw);
        draw.setMainActivity(this);
        draw.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(!drawable) {
                    return false;
                }
                PointPacket pointPacket = new PointPacket(new Point(event.getX(),event.getY(),draw.stroke, draw.color),event.getAction(),receiver.getHardwareAddress());
                ((Draw) v).addPointPacket(pointPacket,paths);
                if(transmission != null)
                    transmission.addPointPacket(pointPacket);
                return true;
            }
        });


        if(savedInstanceState != null) {
            draw.color = savedInstanceState.getInt("color");
            draw.stroke = savedInstanceState.getInt("stroke");
            draw.alpha = savedInstanceState.getInt("alpha");
            connexionMode = savedInstanceState.getInt("connexionMode");
            ArrayList<HardwareAddress> usersHardwareAddress = savedInstanceState.getParcelableArrayList("usersHardwareAddress");
            for (HardwareAddress address : usersHardwareAddress) {
                users.put(address, new DrawTools());
            }
            ArrayList<PointPacket> points = savedInstanceState.getParcelableArrayList("points");
            getDraw().setPoints(points);
        }

        Button refreshButton = (Button) findViewById(R.id.refresh_button);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.this.getDraw().clear();
            }
        });

        initColorButtons();
        initSeekBar();

        wifiP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = wifiP2pManager.initialize(this, getMainLooper(), null);
        receiver = new Receiver(wifiP2pManager, channel, this);
        wifiP2pManager.discoverPeers(channel,receiver.discover);
    }

    //TODO sauvegarde des données
    //sauvegarde du serveur
    //sauvegarde des points
    @Override
    public void onResume(){
        super.onResume();
        //loadingDisplay(true);
        receiver = new Receiver(wifiP2pManager, channel, this);
        registerReceiver(receiver, intentFilter);

        if(connexionMode == LOCAL) {
            loadingDisplay(false);
            return;
        }
/*
        wifiP2pManager.requestConnectionInfo(channel, new WifiP2pManager.ConnectionInfoListener() {
            @Override
            public void onConnectionInfoAvailable(WifiP2pInfo info) {
                if(info.groupFormed) {
                    if(info.isGroupOwner) {
                        ServerP2P server = new ServerP2P(MainActivity.this);
                        setTransmission(server.getSynchronizer());
                        connected = true;
                        loadingDisplay(false);
                    } else {
                         if(getHardwareAddress() != null) {
                             //transmission = new ClientPeer(MainActivity.this, info.groupOwnerAddress,getHardwareAddress());
                         }
                    }
                }
            }
        });
        */
    }

    @Override
    public void onPause(){
        super.onPause();
        unregisterReceiver(receiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        setStop(true);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("color",draw.color);
        outState.putInt("stroke",draw.stroke);
        outState.putInt("alpha",draw.alpha);
        outState.putInt("connexionMode",connexionMode);
        //outState.putParcelableArrayList("paths",paths);
        ArrayList<HardwareAddress> hardwareAddresses = new ArrayList<>(getUsers().keySet());
        outState.putParcelableArrayList("usersHardwareAddress",hardwareAddresses);
        outState.putParcelableArrayList("points",getPoints());
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
    public Receiver getReceiver() {
        return receiver;
    }
    public ArrayList<MyPath> getPaths() {
        return paths;
    }
    public TreeMap<HardwareAddress,DrawTools> getUsers() {
        return users;
    }
    public ArrayList<PointPacket> getPoints() {
        return getDraw().getPoints();
    }
    public void setConnected(boolean connected) {
        this.connected = connected;
        if(connected) {
            loadingDisplay(false);
        } else {
            serverNotConnected();
        }
    }
    public boolean getConnected() {
        return connected;
    }

    public void sizeChangedDraw() {
        draw.drawPaths(paths);
    }

    public void hardwareAddressAvailable() {
        if(!getUsers().containsKey(getHardwareAddress()))
            getUsers().put(getHardwareAddress(),new DrawTools());
    }

    public void onClickPeerDiscovered(View v) {
        receiver.connect(((TextView) v).getText().toString());
    }

    private void setStop(boolean stop) {
        if(transmission != null)
            transmission.setStop(stop);
        //connected = false;
    }

    public void loadingDisplay(boolean isLoading) {
        final boolean loading = isLoading;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(loading) {
                    drawable = false;
                    findViewById(R.id.loading).setVisibility(View.VISIBLE);
                } else {
                    drawable = true;
                    findViewById(R.id.loading).setVisibility(View.INVISIBLE);
                }
            }
        });

    }

    private void initColorButtons() {
        ArrayList<Button> colorButtons = new ArrayList<>();

        colorButtons.add((Button)findViewById(R.id.button_color1));
        colorButtons.add((Button)findViewById(R.id.button_color2));
        colorButtons.add((Button)findViewById(R.id.button_color3));
        colorButtons.add((Button)findViewById(R.id.button_color4));
        colorButtons.add((Button)findViewById(R.id.button_color5));
        colorButtons.add((Button)findViewById(R.id.button_color6));
        colorButtons.add((Button)findViewById(R.id.button_color7));
        colorButtons.add((Button)findViewById(R.id.button_color8));
        colorButtons.add((Button)findViewById(R.id.button_color9));
        colorButtons.add((Button)findViewById(R.id.button_color10));
        colorButtons.add((Button)findViewById(R.id.button_color11));
        colorButtons.add((Button)findViewById(R.id.button_color12));

        for(Button button: colorButtons) {
           button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Button selectedColor = (Button) findViewById(R.id.selected_color);
                    String colorString = ((Button) view).getText().toString();
                    int color = Color.parseColor(colorString);
                    draw.color = color;

                    selectedColor.setBackgroundColor(color);
                }
            });

        }
    }

    public void initSeekBar() {
        SeekBar seekBar = (SeekBar) findViewById(R.id.stroke_bar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                draw.stroke = i;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekBar = (SeekBar) findViewById(R.id.trans_bar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                draw.alpha = i;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(data.getBooleanExtra("refresh",false)) {
            wifiP2pManager.discoverPeers(channel,receiver.discover);
            loadingDisplay(true);
            return;
        }

        if(!data.getBooleanExtra("localMode",true)) {
            if(data.getBooleanExtra("serverMode",false)) {

                ServerP2P server = new ServerP2P(MainActivity.this);
                setTransmission(server.getSynchronizer());
                setConnected(true);
                //connexionMode = SERVER;
            } else {
                receiver.connect(data.getStringExtra("deviceName"));
                /*if(connected)
                    connexionMode = CLIENT;*/
            }
        } else {
            connexionMode = LOCAL;
            setConnected(true);
            return;
        }
        System.out.println("in MainActivity: " + mainActivity.getConnected());

/*
        System.out.println("in ActivityResult: " + connected);
        if(getConnected())
            loadingDisplay(false);
        else {
            serverNotConnected();
        }
        */
    }

    private void serverNotConnected() {
        loadingDisplay(true);
        startConnexionActivity = false;
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Connexion failed !")
                .setMessage("Make sure the group owner is connected in Server Mode.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startConnexionActivity = true;
                        wifiP2pManager.discoverPeers(channel,receiver.discover);
                    }
                })
                .show();
    }

    public void startConnexionActivity(String[] peersName) {
        if(!startConnexionActivity)
            return;

        Intent intent = new Intent(mainActivity, ConnexionActivity.class);

        String[] names = peersName;
        intent.putExtra("peersName", names);

        mainActivity.startActivityForResult(intent, 1);
        mainActivity.loadingDisplay(false);
    }

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

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
}
