package com.example.adrien.projetmobilel3.activities;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Color;
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
import android.widget.Toast;

import com.example.adrien.projetmobilel3.R;
import com.example.adrien.projetmobilel3.common.DrawTools;
import com.example.adrien.projetmobilel3.common.HardwareAddress;
import com.example.adrien.projetmobilel3.common.MyPath;
import com.example.adrien.projetmobilel3.common.PointPacket;
import com.example.adrien.projetmobilel3.common.PointTransmission;
import com.example.adrien.projetmobilel3.draw.Draw;
import com.example.adrien.projetmobilel3.common.Point;
import com.example.adrien.projetmobilel3.server.ServerP2P;

import java.util.ArrayList;
import java.util.TreeMap;

//TODO (optionnel) sauveguarde du dessin pour les nouveaux


/**
 * The DrawActivity class is the main activity of the application.
 * This activity is launched at the starting of the application
 * and will display a loading screen waiting for network information.
 * When network information are available, it will starts ConnectionActivity
 * to select the connection mode. (Note that it's not always the case, check
 * the ConnectionActivity documentation for more).
 * When the ConnectionActivity returns a correct result, the user can draw.
 */
public class DrawActivity extends Activity {

    /**
     * Means that the user is in local mode.
     */
    public static final int LOCAL = 1;

    /**
     * Means that the user is in server mode.
     */
    public static final int SERVER = 2;

    /**
     * Means that the user is in client mode.
     */
    public static final int CLIENT = 3;

    /**
     * Means that the user has not selected a mode.
     */
    public static final int NONE = 0;

    /**
     * Initialized at creation, it will catch relevant intent for the activity.
     */
    private final IntentFilter intentFilter = new IntentFilter();

    /**
     * Initialized at creation, it provides an interface to interact with
     * other devices with WIFI P2P.
     */
    private WifiP2pManager wifiP2pManager = null;

    /**
     * WIFI P2P Channel
     */
    private Channel channel = null;

    /**
     * WIFI P2P Receiver.
     * Check Receiver documentation for more.
     */
    private Receiver receiver = null;

    /**
     * The point transmission.
     * Check PointTransmission documentation for more.
     */
    private PointTransmission transmission;

    /**
     * The current connection mode.
     */
    public int connectionMode = NONE;

    /**
     * Indicate if the WIFI P2P is enabled.
     */
    private boolean isWifiP2pEnabled = false;

    /**
     * A link to this activity, to ease link transmission.
     */
    public final DrawActivity drawActivity = this;

    /**
     * Indicate if the user is properly connected, in local or network mode.
     */
    public boolean connected = false;

    /**
     * Indicate if the user can draw.
     * Usually it's false when the loading screen is displayed.
     */
    private boolean drawable = false;

    /**
     * Indicate if the connection activity can be started.
     * Usually it's false when an error message is not confirmed by the user.
     */
    private boolean startConnectionActivity = true;

    private boolean automaticReconnection = false;

    /**
     * The link to the draw view.
     */
    private Draw draw;

    /**
     * A list of paths containing all known points.
     * These points are savec when the activity need to restart.
     */
    private ArrayList<MyPath> paths = new ArrayList<>();

    /**
     * A tree map to attribute draw tools to each user.
     * This is required when you have multiple users.
     */
    private final TreeMap<HardwareAddress,DrawTools> users = new TreeMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw);

        loadingDisplay(true);

        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.activity_draw);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, null, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        draw = (Draw) findViewById(R.id.draw);
        draw.setDrawActivity(this);
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
            connectionMode = savedInstanceState.getInt("connectionMode");
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
                DrawActivity.this.getDraw().clear();
            }
        });

        initColorButtons();
        initSeekBar();

        wifiP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = wifiP2pManager.initialize(this, getMainLooper(), null);
        receiver = new Receiver(wifiP2pManager, channel, this);
        wifiP2pManager.discoverPeers(channel,receiver.discover);
    }

    //TODO sauvegarde des données améliorée
    @Override
    public void onResume(){
        super.onResume();
        //loadingDisplay(true);
        receiver = new Receiver(wifiP2pManager, channel, this);
        registerReceiver(receiver, intentFilter);

        if(connectionMode == LOCAL) {
            loadingDisplay(false);
            return;
        }
/*
        wifiP2pManager.requestConnectionInfo(channel, new WifiP2pManager.ConnectionInfoListener() {
            @Override
            public void onConnectionInfoAvailable(WifiP2pInfo info) {
                if(info.groupFormed) {
                    if(info.isGroupOwner) {
                        ServerP2P server = new ServerP2P(DrawActivity.this);
                        connected = true;
                        loadingDisplay(false);
                    } else {
                         if(getHardwareAddress() != null) {
                             //transmission = new ClientPeer(DrawActivity.this, info.groupOwnerAddress,getHardwareAddress());
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
        wifiP2pManager.removeGroup(channel,null);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("color",draw.color);
        outState.putInt("stroke",draw.stroke);
        outState.putInt("alpha",draw.alpha);
        outState.putInt("connectionMode", connectionMode);
        //ArrayList<HardwareAddress> hardwareAddresses = new ArrayList<>(getUsers().keySet());
        //outState.putParcelableArrayList("usersHardwareAddress",hardwareAddresses);
        outState.putParcelableArrayList("points",getPoints());
    }

    /**
     * Set the WIFI P2P state.
     * If it's false, the user is notified with a message.
     * @param isWifiP2pEnabled True if the WIFI P2P is enabled.
     */
    public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled){
        this.isWifiP2pEnabled = isWifiP2pEnabled;
        if(isWifiP2pEnabled) {
           // Toast.makeText(this, "WIFI P2P enabled.", Toast.LENGTH_SHORT).show();
        } else {
            loadingDisplay(false);
           Toast.makeText(this, "WIFI P2P disabled.", Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * Indicates if the WIFI P2P is enabled.
     * @return True if the wifi P2P is enabled.
     */
    public boolean isWifiP2pEnabled() {
        return isWifiP2pEnabled;
    }

    /**
     * Set the new transmission.
     * Can be null, if it's the case, new points will only be local.
     * @param transmission The new transmission.
     */
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
    public void setAutomaticReconnection(boolean automaticReconnection) {
        this.automaticReconnection = automaticReconnection;
    }
    public boolean getAutomaticReconnection() {
        return automaticReconnection;
    }

    /**
     * Set the connection state.
     * If true, the loading screen is turned off and the user can draw.
     * Else, the connection to the server failed and an error message is sent.
     * @param connected True if the connection to the server is successful.
     */
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

    /**
     * When the screen changes, usually when it rotates,
     * this method is called by the draw view to re-draw old points.
     */
    public void sizeChangedDraw() {
        draw.drawPaths(paths);
    }

    /**
     * This method is called when the hardware address of the device is available,
     * and set it.
     */
    public void hardwareAddressAvailable() {
        if(!getUsers().containsKey(getHardwareAddress()))
            getUsers().put(getHardwareAddress(),new DrawTools());
    }

    /**
     * Called when the user click on a device name and connect to it.
     */
    public void onClickPeerDiscovered(View v) {
        receiver.connect(((TextView) v).getText().toString());
    }

    public void onClickConnectionMode(View v) {
        startConnectionActivity = true;
        connected = false;
        loadingDisplay(true);
        wifiP2pManager.discoverPeers(channel,receiver.discover);
    }

    /**
     * Stop the transmission if exists.
     * @param stop True to stop the transmission.
     */
    private void setStop(boolean stop) {
        if(transmission != null)
            transmission.setStop(stop);
        //connected = false;
    }

    /**
     * Enable or disable the loading screen.
     * While the loading screen is enabled, the user can't draw.
     * @param isLoading True to enable the loading screen.
     */
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

    /**
     * Initialize listener of all buttons of color selection.
     */
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

    /**
     * Initialize listener of all seek bar.
     *
     */
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

    /**
     * Called when the ConnectionActivity returns and handle the result.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        startConnectionActivity = true;
        if(data.getBooleanExtra("refresh",false)) {
            wifiP2pManager.discoverPeers(channel,receiver.discover);
            loadingDisplay(true);
            return;
        }

        if(!data.getBooleanExtra("localMode",true)) {
            if(data.getBooleanExtra("serverMode",false)) {

                ServerP2P server = new ServerP2P(DrawActivity.this);
                setConnected(true);
                //connectionMode = SERVER;
            } else {
                startConnectionActivity = false;
                receiver.connect(data.getStringExtra("deviceName"));
                /*if(connected)
                    connectionMode = CLIENT;*/
            }
        } else {
            connectionMode = LOCAL;
            setConnected(true);
            setAutomaticReconnection(false);
            return;
        }
        System.out.println("in DrawActivity: " + drawActivity.getConnected());


/*
        System.out.println("in ActivityResult: " + connected);
        if(getConnected())
            loadingDisplay(false);
        else {
            serverNotConnected();
        }
        */
    }

    /**
     * Called when the connection to the server has failed.
     * The loading screen is enabled and an alert is sent.
     * The user must click on OK to restart a discovery and the connection activity.
     */
    private void serverNotConnected() {
        loadingDisplay(true);
        startConnectionActivity = false;
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(R.string.connection_failed)
                .setMessage(R.string.group_owner_server_mode)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startConnectionActivity = true;
                        wifiP2pManager.discoverPeers(channel,receiver.discover);
                    }
                })
                .show();
    }

    /**
     * Start the connection activity with given parameters.
     * Check if the activity can be started and return if not.
     * @param peersName The string array containing all found devices.
     *                  Size can be 0.
     * @param groupInformation The string array containing information about the group.
     *                  Size can be 0.
     */
    public void startConnectionActivity(String[] peersName, String[] groupInformation) {
        if(!startConnectionActivity)
            return;

        automaticReconnection = true;
        startConnectionActivity = false;
        Intent intent = new Intent(drawActivity, ConnectionActivity.class);

        String[] names = peersName;
        intent.putExtra("peersName", names);
        intent.putExtra("groupInformation",groupInformation);
        intent.putExtra("isWifiP2pEnabled",isWifiP2pEnabled);

        drawActivity.startActivityForResult(intent, 1);
        drawActivity.loadingDisplay(false);
    }

    /**
     * Close the draw settings view if activated.
     * Else call super.onBackPressed.
     */
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.activity_draw);
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
