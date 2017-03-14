package com.example.adrien.projetmobilel3;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
//import android.util.Log;
import android.os.AsyncTask;
import android.support.v7.app.NotificationCompat;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.adrien.projetmobilel3.client.ClientPeer;
import com.example.adrien.projetmobilel3.common.HardwareAddress;
import com.example.adrien.projetmobilel3.draw.Point;
import com.example.adrien.projetmobilel3.server.ServerP2P;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static android.content.Context.NOTIFICATION_SERVICE;
import static android.net.ConnectivityManager.TYPE_WIFI;

/**
 * Created by Adrien on 20/02/2017.
 */

public class Receiver extends BroadcastReceiver {

    private WifiP2pManager wifiP2pManager;
    private Channel channel;
    private HardwareAddress hardwareAddress;
    private MainActivity mainActivity;

    private List<WifiP2pDevice> peers = new ArrayList<>();
    private HashMap<String,String> peersInfo = new HashMap<>();

    private PeerListListener peerListListener = new PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList newPeers) {
            List<WifiP2pDevice> refreshedPeers = new ArrayList<> (newPeers.getDeviceList());
            if(!refreshedPeers.equals(peers)){
                peers.clear();
                peers.addAll(refreshedPeers);

                //notify changement
            }

            ListView lv = (ListView) mainActivity.findViewById(R.id.peersList);
            //ArrayAdapter<WifiP2pDevice> aas = new ArrayAdapter<>(mainActivity,R.layout.peer_item_adapter);
            ArrayAdapter<String> as = new ArrayAdapter<>(mainActivity,R.layout.peer_item_adapter);
            lv.setAdapter(as);
            for(WifiP2pDevice device: peers) {
                peersInfo.put(device.deviceName,device.deviceAddress);
                as.add(device.deviceName);
            }

            if(peers.size() == 0){
                as.add("No device found");
                return;
            } else {

            }
        }
    };


    final WifiP2pManager.ActionListener discoverThenConnect = new WifiP2pManager.ActionListener() {
        @Override
        public void onSuccess() {
            connect();
        }

        @Override
        public void onFailure(int reason) {
            Toast.makeText(mainActivity, "Discover failed. Check your WIFI connexion", Toast.LENGTH_SHORT).show();
        }
    };

    final WifiP2pManager.ActionListener discover= new WifiP2pManager.ActionListener() {
        @Override
        public void onSuccess() {}

        @Override
        public void onFailure(int reason) {
            Toast.makeText(mainActivity, "Discover failed. Check your WIFI connexion", Toast.LENGTH_SHORT).show();
        }
    };

    private ConnectionInfoListener connectionInfoListener = new ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(final WifiP2pInfo info) {
            if(info.groupFormed) {
                if(info.isGroupOwner){
                    Toast.makeText(mainActivity, "Group owner", Toast.LENGTH_SHORT).show();
                    ServerP2P server = new ServerP2P(mainActivity);
                    mainActivity.setTransmission(server.getSynchronizer());
                    // Do whatever tasks are specific to the group owner.
                    // One common case is creating a group owner thread and accepting
                    // incoming connections.
                } else {
                    Toast.makeText(mainActivity, "client", Toast.LENGTH_SHORT).show();
                    if(mainActivity.getTransmission() != null)
                        mainActivity.getTransmission().setStop(true);
                    if(hardwareAddress != null)
                        mainActivity.setTransmission(new ClientPeer(mainActivity,info.groupOwnerAddress,hardwareAddress));
                    else {
                        try {
                            throw new Exception("Hardware address unknown");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    // The other device acts as the peer (client). In this case,
                    // you'll want to create a peer thread that connects
                    // to the group owner.
                }
            }
        }
    };

    public Receiver(WifiP2pManager wifiP2pManager, Channel channel, MainActivity activity){
        this.wifiP2pManager = wifiP2pManager;
        this.channel = channel;
        this.mainActivity = activity;
        buttons();
    }

    public HardwareAddress getHardwareAddress() {
        return hardwareAddress;
    }

    //TODO: boutons à enlever
    // mais pratique pour tester rapidement
    private void buttons() {
        Button groupInfoButton = (Button) mainActivity.findViewById(R.id.group_info_button);
        groupInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wifiP2pManager.requestGroupInfo(mainActivity.getChannel(), new WifiP2pManager.GroupInfoListener() {
                    @Override
                    public void onGroupInfoAvailable(WifiP2pGroup group) {
                        if (group != null) {
                            ListView lv = (ListView) mainActivity.findViewById(R.id.peersList);
                            ArrayAdapter<WifiP2pDevice> aas = new ArrayAdapter<>(mainActivity, R.layout.peer_item_adapter);
                            ArrayAdapter<String> as = new ArrayAdapter<>(mainActivity, R.layout.peer_item_adapter);
                            lv.setAdapter(as);
                            as.add("Group owner: " + group.getOwner().deviceName + " -- " + group.getOwner().deviceAddress);
                            for (WifiP2pDevice device : peers)
                                as.add(device.deviceName);
                        } else {
                            Toast.makeText(mainActivity,"You're not part of a group", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
/*
        Button sendToGroupOwner = (Button) mainActivity.findViewById(R.id.send_to_group_owner);
        sendToGroupOwner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wifiP2pManager.requestConnectionInfo(channel, new ConnectionInfoListener() {
                    @Override
                    public void onConnectionInfoAvailable(WifiP2pInfo info) {
                        ((TextView) mainActivity.findViewById(R.id.sendStatus)).setText("...");
                        new AsyncTask<InetAddress, Object, Boolean>() {
                                    @Override
                                    protected Boolean doInBackground(InetAddress... params) {
                                        ConnectivityManager cm = (ConnectivityManager) (mainActivity.getSystemService(Context.CONNECTIVITY_SERVICE));
                                        if(cm != null
                                            && cm.getActiveNetworkInfo().isConnected())  {
                                            try {
                                                byte[] bytes;
                                                Socket socket = new Socket(params[0], ServerP2P.DEFAULT_PORT);
                                                Point point = new Point(30,30,50, Color.RED);
                                                socket.getOutputStream().write(point.getBytes());
                                                socket.getInputStream().read(new byte[1]);
                                                socket.close();
                                                return true;
                                            } catch (SocketException e) {
                                                e.printStackTrace();
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                        return false;
                                    }

                                    @Override
                                    protected void onPostExecute(Boolean aBoolean) {
                                        super.onPostExecute(aBoolean);
                                        if(aBoolean) {
                                            ((TextView) mainActivity.findViewById(R.id.sendStatus)).setText("Sent");
                                            NotificationCompat.Builder builder = new NotificationCompat.Builder(mainActivity);
                                            builder.setContentTitle("Message received")
                                                    .setContentText("Message received from the server")
                                                    .setSmallIcon(R.drawable.message_received);
                                            NotificationManager nm = (NotificationManager) mainActivity.getSystemService(NOTIFICATION_SERVICE);
                                            nm.notify(0, builder.build());
                                        } else {
                                            ((TextView) mainActivity.findViewById(R.id.sendStatus)).setText("Not sent ");
                                        }
                                    }
                                }.execute(info.groupOwnerAddress);
                }
            });
        }
    });*/
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)){
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if(state == WifiP2pManager.WIFI_P2P_STATE_ENABLED){
                mainActivity.setIsWifiP2pEnabled(true);
            } else {
                mainActivity.setIsWifiP2pEnabled(false);
            }
        } else if(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)){

            if(wifiP2pManager != null){
                wifiP2pManager.requestPeers(channel, peerListListener);
            }
            //Log.d(WiFiDirectActivity.TAG; "P2P peers changed");
/*
            NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            if(networkInfo.isConnected()){
                //Connected to an other device
                //info to find group owner IP

                wifiP2pManager.requestConnectionInfo(channel, connectionInfoListener);
            }*/

        } else if(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)){

        } else if(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)){
            WifiP2pDevice device = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
            hardwareAddress = HardwareAddress.parseHardwareAddress(device.deviceAddress);
        }
    }

    public void connect(){

        if(peers.size() == 0)
            return;

        for(WifiP2pDevice device:peers) {
            WifiP2pConfig config = new WifiP2pConfig();
            config.deviceAddress = device.deviceAddress;
            config.wps.setup = WpsInfo.PBC;

            wifiP2pManager.connect(channel, config, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    wifiP2pManager.requestConnectionInfo(channel, connectionInfoListener);
                }

                @Override
                public void onFailure(int reason) {
                    Toast.makeText(mainActivity, "Connection failed. Retry.", Toast.LENGTH_SHORT)
                            .show();
                }
            });
        }
    }

    public void connect(String deviceName){
            WifiP2pConfig config = new WifiP2pConfig();
            config.deviceAddress = peersInfo.get(deviceName);
            config.wps.setup = WpsInfo.PBC;

            wifiP2pManager.connect(channel, config, new WifiP2pManager.ActionListener() {
                        @Override
                        public void onSuccess() {
                            wifiP2pManager.requestConnectionInfo(channel, connectionInfoListener);
                        }

                        @Override
                        public void onFailure(int reason) {
                            Toast.makeText(mainActivity, "Connection failed. Retry.", Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }
            );
    }
}
