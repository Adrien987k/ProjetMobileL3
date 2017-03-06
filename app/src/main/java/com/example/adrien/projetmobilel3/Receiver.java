package com.example.adrien.projetmobilel3;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
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
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.adrien.projetmobilel3.server.ServerP2P;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by Adrien on 20/02/2017.
 */

public class Receiver extends BroadcastReceiver {

    private WifiP2pManager wifiP2pManager;
    private Channel channel;
    private MainActivity mainActivity;

    private List<WifiP2pDevice> peers = new ArrayList<>();

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
            for(WifiP2pDevice device: peers)
                as.add(device.deviceName);

            if(peers.size() == 0){
                as.add("No device found");
                return;
            } else {

            }
        }
    };

    private ConnectionInfoListener connectionInfoListener = new ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(final WifiP2pInfo info) {
            InetAddress groupOwnerAddress = null;
            try {
                groupOwnerAddress = InetAddress.getByName(info.groupOwnerAddress.getHostAddress());
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            if(info.groupFormed && info.isGroupOwner){
                Toast.makeText(mainActivity, "Group owner", Toast.LENGTH_SHORT).show();
                ServerP2P server = new ServerP2P(mainActivity);
                // Do whatever tasks are specific to the group owner.
                // One common case is creating a group owner thread and accepting
                // incoming connections.
            } else if(info.groupFormed){
                Toast.makeText(mainActivity, "client", Toast.LENGTH_SHORT).show();
                if(groupOwnerAddress != null) {
                    new AsyncTask<InetAddress, Object, Boolean>() {
                        @Override
                        protected Boolean doInBackground(InetAddress... params) {
                            try {

                                byte[] bytes;
                                Socket socket = new Socket(params[0],ServerP2P.DEFAULT_PORT);
                                socket.getOutputStream().write(new byte[] {1,1});
                                /*
                                DatagramSocket ds = new DatagramSocket();
                                DatagramPacket packet = new DatagramPacket((bytes = new byte[]{1, 1, 1, 1, 0})
                                        , bytes.length
                                        , params[0]
                                        , ServerP2P.DEFAULT_PORT);
                                ds.send(packet);
                                ds.close();*/

                                socket.getInputStream().read(new byte[1]);
                                return true;
                            } catch (SocketException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            return false;
                        }

                        @Override
                        protected void onPostExecute(Boolean aBoolean) {
                            super.onPostExecute(aBoolean);
                            mainActivity.isSent(aBoolean);
                        }
                    }.execute(groupOwnerAddress);
                } else {
                    AlertDialog.Builder alert = new AlertDialog.Builder(mainActivity);
                    alert.setTitle("Unknown Address").show();
                }
                // The other device acts as the peer (client). In this case,
                // you'll want to create a peer thread that connects
                // to the group owner.
            }
        }
    };

    public Receiver(WifiP2pManager wifiP2pManager, Channel channel, MainActivity activity){
        this.wifiP2pManager = wifiP2pManager;
        this.channel = channel;
        this.mainActivity = activity;

        buttons();

    }


    private void buttons() {
        Button groupInfoButton = (Button) mainActivity.findViewById(R.id.group_info_button);
        groupInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wifiP2pManager.requestGroupInfo(mainActivity.channel, new WifiP2pManager.GroupInfoListener() {
                    @Override
                    public void onGroupInfoAvailable(WifiP2pGroup group) {
                        ListView lv = (ListView) mainActivity.findViewById(R.id.peersList);
                        ArrayAdapter<WifiP2pDevice> aas = new ArrayAdapter<>(mainActivity,R.layout.peer_item_adapter);
                        ArrayAdapter<String> as = new ArrayAdapter<>(mainActivity,R.layout.peer_item_adapter);
                        lv.setAdapter(as);
                        as.add("Group: " + group);
                        as.add("Group owner: "+ group.getOwner().deviceName + " -- " + group.getOwner().deviceAddress);
                        for(WifiP2pDevice device:peers)
                            as.add(device.deviceName);
                    }
                });
            }
        });

        Button sendToGroupOwner = (Button) mainActivity.findViewById(R.id.send_to_group_owner);
        sendToGroupOwner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wifiP2pManager.requestConnectionInfo(channel, new ConnectionInfoListener() {
                    @Override
                    public void onConnectionInfoAvailable(WifiP2pInfo info) {
                        new AsyncTask<InetAddress,Object,Object>() {
                            @Override
                            protected Object doInBackground(InetAddress... params) {
                                try {

                                    byte[] bytes;
                                    DatagramSocket ds = new DatagramSocket();
                                    DatagramPacket packet = new DatagramPacket((bytes = new byte[] {1,1,1,1,0})
                                            ,bytes.length
                                            ,params[0]
                                            ,ServerP2P.DEFAULT_PORT);
                                    ds.send(packet);
                                    ds.close();
                                } catch (SocketException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                return null;
                            }
                        }.execute(info.groupOwnerAddress);
                }
            });
        }
    });
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
            //DeviceListFragment fragment = (DeviceList)
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
            }

            );
        }

    }

}
