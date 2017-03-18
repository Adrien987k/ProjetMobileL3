package com.example.adrien.projetmobilel3.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
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
import android.widget.Toast;

import com.example.adrien.projetmobilel3.client.ClientPeer;
import com.example.adrien.projetmobilel3.common.HardwareAddress;
import com.example.adrien.projetmobilel3.server.ServerP2P;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Adrien on 20/02/2017.
 */

public class Receiver extends BroadcastReceiver {

    //TODO chargement infini à la première connexion

    private WifiP2pManager wifiP2pManager;
    private Channel channel;
    private HardwareAddress hardwareAddress;
    private DrawActivity drawActivity;

    private List<WifiP2pDevice> peers = new ArrayList<>();
    private HashMap<String,String> peersInfo = new HashMap<>();

    private PeerListListener peerListListener = new PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList newPeers) {
            List<WifiP2pDevice> refreshedPeers = new ArrayList<> (newPeers.getDeviceList());
            if(!refreshedPeers.equals(peers)){
                peers.clear();
                peers.addAll(refreshedPeers);

                ArrayList<String> peersName = new ArrayList<>();
                for(WifiP2pDevice device: peers) {
                    peersInfo.put(device.deviceName,device.deviceAddress);
                    peersName.add(device.deviceName);
                }
                /*
                if(!drawActivity.connected
                        && drawActivity.connexionMode != DrawActivity.LOCAL) {
                   drawActivity.startConnectionActivity(peersName.toArray(new String[peersName.size()]));
                }
                */

                if(!drawActivity.connected
                        && drawActivity.connexionMode != DrawActivity.LOCAL) {
                    wifiP2pManager.requestGroupInfo(channel, new WifiP2pManager.GroupInfoListener() {
                        @Override
                        public void onGroupInfoAvailable(WifiP2pGroup group) {

                            if (group != null) {
                                ArrayList<String> groupInfo = new ArrayList<>();
                                for (WifiP2pDevice client : group.getClientList())
                                    groupInfo.add(client.deviceName);

                                drawActivity.startConnectionActivity(peersInfo.keySet().toArray(new String[peersInfo.keySet().size()])
                                        , groupInfo.toArray(new String[groupInfo.size()])
                                );
                            } else {
                                drawActivity.startConnectionActivity(peersInfo.keySet().toArray(new String[peersInfo.keySet().size()])
                                        , new String[0]
                                );
                            }
                        }
                    });
                }
            }

        }
    };

final WifiP2pManager.ActionListener discover = new WifiP2pManager.ActionListener() {
        @Override
        public void onSuccess() {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(20000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if(peers.size() == 0
                               && !drawActivity.connected) {
                            wifiP2pManager.discoverPeers(channel,discover);
                       }

                }
            }).start();
        }

        @Override
        public void onFailure(int reason) {
            //Toast.makeText(drawActivity, "Discover impossible. Check your WIFI connexion and refresh. ", Toast.LENGTH_SHORT).show();
            drawActivity.startConnectionActivity(new String[0],new String[0]);
        }
    };

    private ConnectionInfoListener connectionInfoListener = new ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(final WifiP2pInfo info) {
            wifiP2pManager.stopPeerDiscovery(channel,null);
            if(info.groupFormed) {
                if(info.isGroupOwner){
                    Toast.makeText(drawActivity, "You are the group owner", Toast.LENGTH_SHORT).show();
                    ServerP2P server = new ServerP2P(drawActivity);
                    drawActivity.setTransmission(server.getSynchronizer());
                    drawActivity.setConnected(true);
                    drawActivity.connexionMode = DrawActivity.SERVER;
                    // Do whatever tasks are specific to the group owner.
                    // One common case is creating a group owner thread and accepting
                    // incoming connections.
                } else {
                    Toast.makeText(drawActivity, "You are a group member", Toast.LENGTH_SHORT).show();
                    if(drawActivity.getTransmission() != null)
                        drawActivity.getTransmission().setStop(true);
                    if(hardwareAddress != null) {
                        ClientPeer client = new ClientPeer(drawActivity, info.groupOwnerAddress, hardwareAddress);
                        if(client.connexionEstablished()) {
                            drawActivity.setTransmission(client);
                            drawActivity.setConnected(true);
                            drawActivity.connexionMode = DrawActivity.CLIENT;
                        } else  {
                            drawActivity.setConnected(false);
                        }
                        System.out.println("in Receiver: " + drawActivity.getConnected());
                    }
                    else {
                        throw new IllegalStateException("Hardware address unknown");
                    }
                    // The other device acts as the peer (client). In this case,
                    // you'll want to create a peer thread that connects
                    // to the group owner.
                }
            }
        }
    };

    public Receiver(WifiP2pManager wifiP2pManager, Channel channel, DrawActivity activity){
        this.wifiP2pManager = wifiP2pManager;
        this.channel = channel;
        this.drawActivity = activity;
    }

    public HardwareAddress getHardwareAddress() {
        return hardwareAddress;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)){
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if(state == WifiP2pManager.WIFI_P2P_STATE_ENABLED){
                drawActivity.setIsWifiP2pEnabled(true);
            } else {
                drawActivity.setIsWifiP2pEnabled(false);
            }
        } else if(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)){
                wifiP2pManager.requestPeers(channel, peerListListener);

            NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            if(networkInfo != null
                &&networkInfo.isConnected()) {
                //Connected to an other device
                //info to find group owner IP

                wifiP2pManager.requestConnectionInfo(channel, connectionInfoListener);
            }

        } else if(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)){
            //wifiP2pManager.requestConnectionInfo(channel,connectionInfoListener);

        } else if(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)){
            WifiP2pDevice device = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
            setHardwareAddress(HardwareAddress.parseHardwareAddress(device.deviceAddress));
        }
    }

    public void connect(String deviceName){
        if(deviceName.equals("No device found")
                || deviceName.equals("You are not part of a group"))
            return;

            WifiP2pConfig config = new WifiP2pConfig();
            config.deviceAddress = peersInfo.get(deviceName);
            config.wps.setup = WpsInfo.PBC;

            wifiP2pManager.connect(channel, config, new WifiP2pManager.ActionListener() {
                        @Override
                        public void onSuccess() {
                            //drawActivity.loadingDisplay(true);
                            wifiP2pManager.requestConnectionInfo(channel, connectionInfoListener);
                        }

                        @Override
                        public void onFailure(int reason) {
                            Toast.makeText(drawActivity, "Connexion failed.", Toast.LENGTH_SHORT)
                                    .show();
                            wifiP2pManager.discoverPeers(channel,discover);
                        }
                    }
            );
    }

    private void setHardwareAddress(HardwareAddress hardwareAddress) {
        this.hardwareAddress = hardwareAddress;
        drawActivity.hardwareAddressAvailable();
    }
    public ArrayList<String> getPeersName() {
        return new ArrayList<>(peersInfo.keySet());
    }
}
