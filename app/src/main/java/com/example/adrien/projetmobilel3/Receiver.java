package com.example.adrien.projetmobilel3;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
//import android.util.Log;
import android.widget.Toast;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Adrien on 20/02/2017.
 */

public class Receiver extends BroadcastReceiver {

    private WifiP2pManager wifiP2pManager;
    private Channel channel;
    private MainActivity activity;

    private List<WifiP2pDevice> peers = new ArrayList<>();

    private PeerListListener peerListListener = new PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList newPeers) {
            List<WifiP2pDevice> refreshedPeers = (List<WifiP2pDevice>) newPeers.getDeviceList();
            if(!refreshedPeers.equals(peers)){
                peers.clear();
                peers.addAll(refreshedPeers);

                //notify changement
            }

            if(peers.size() == 0){
                //Log.d(WiFiDirectActivity.TAG, "No Devices found");
                return;
            }
        }
    };

    private ConnectionInfoListener connectionInfoListener = new ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(final WifiP2pInfo info) {
            try {
                InetAddress groupOwnerAddress = InetAddress.getByName(info.groupOwnerAddress.getHostAddress());
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            if(info.groupFormed && info.isGroupOwner){
                // Do whatever tasks are specific to the group owner.
                // One common case is creating a group owner thread and accepting
                // incoming connections.
            } else if(info.groupFormed){
                // The other device acts as the peer (client). In this case,
                // you'll want to create a peer thread that connects
                // to the group owner.
            }
        }
    };

    public Receiver(WifiP2pManager wifiP2pManager, Channel channel, MainActivity activity){
        this.wifiP2pManager = wifiP2pManager;
        this.channel = channel;
        this.activity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)){
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if(state == WifiP2pManager.WIFI_P2P_STATE_ENABLED){
                activity.setIsWifiP2pEnabled(true);
            } else {
                activity.setIsWifiP2pEnabled(false);
            }
        } else if(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)){

            if(wifiP2pManager != null){
                wifiP2pManager.requestPeers(channel, peerListListener);
            }
            //Log.d(WiFiDirectActivity.TAG; "P2P peers changed");

            NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            if(networkInfo.isConnected()){
                //Connected to an other device
                //info to find group owner IP

                wifiP2pManager.requestConnectionInfo(channel, connectionInfoListener);
            }

        } else if(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)){

        } else if(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)){
            //DeviceListFragment fragment = (DeviceList)
        }
    }

    public void connect(){

        WifiP2pDevice device = peers.get(0);

        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;

        wifiP2pManager.connect(channel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(activity, "Connect failed. Retry.", Toast.LENGTH_SHORT)
                        .show();
            }
        });

    }

}
