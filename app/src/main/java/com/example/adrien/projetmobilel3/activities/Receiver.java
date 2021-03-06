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
import android.widget.Toast;

import com.example.adrien.projetmobilel3.R;
import com.example.adrien.projetmobilel3.client.ClientPeer;
import com.example.adrien.projetmobilel3.common.HardwareAddress;
import com.example.adrien.projetmobilel3.server.ServerP2P;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * The Receiver class manage WIFI P2P connection.
 * It contains some listener to handle discovery result
 * or to connect to another P2P device.
 */
public class Receiver extends BroadcastReceiver {

    /**
     * Provide an interface to interact with other P2P devices.
     */
    private WifiP2pManager wifiP2pManager;

    /**
     * WIFI P2P Channel.
     */
    private Channel channel;

    /**
     * The hardware address of the device.
     */
    private HardwareAddress hardwareAddress;

    /**
     * The link to the draw activity.
     */
    private DrawActivity drawActivity;

    /**
     * The list of others P2P devices.
     */
    private List<WifiP2pDevice> peers = new ArrayList<>();

    /**
     * The hash map between name of others P2P devices and their address.
     */
    private HashMap<String,String> peersInfo = new HashMap<>();

    /**
     * Listener used when the peers list has changed.
     * If the user is not yet connected, it will start the connection activity.
     */
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
            }
            if(!drawActivity.connected
                    && drawActivity.connectionMode != DrawActivity.LOCAL) {
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
    };

    /**
     * Listener used when a discovery is started.
     * If the discovery failed, it will start the connection activity immediately.
     * If not, the activity will start when the peer list will be updated.
     * A discovery is started every 20 seconds if the connection activity is not aleady started.
     */
    public final WifiP2pManager.ActionListener discover = new WifiP2pManager.ActionListener() {
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
                            drawActivity.startConnectionActivity(new String[0],new String[0]);
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

    /**
     * Listener used when the connection to a peer is successful.
     * Note that it's the P2P connection and not the server connection.
     * If the user is the group owner, a server will be created.
     * If he'is a member, he will try to connect to the server.
     * An error message is sent if the connection to the server failed.
     */
    private final ConnectionInfoListener connectionInfoListener = new ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(final WifiP2pInfo info) {
            //wifiP2pManager.stopPeerDiscovery(channel,null);
            if(info.groupFormed) {
                if(info.isGroupOwner){
                    Toast.makeText(drawActivity, R.string.you_are_group_owner, Toast.LENGTH_SHORT).show();
                    ServerP2P server = new ServerP2P(drawActivity);
                    drawActivity.setConnected(true);
                    drawActivity.connectionMode = DrawActivity.SERVER;
                    // Do whatever tasks are specific to the group owner.
                    // One common case is creating a group owner thread and accepting
                    // incoming connections.
                } else {
                    Toast.makeText(drawActivity, R.string.you_are_group_member, Toast.LENGTH_SHORT).show();
                    if(drawActivity.getTransmission() != null)
                        drawActivity.getTransmission().setStop(true);
                    if(hardwareAddress != null) {
                        ClientPeer client = new ClientPeer(drawActivity, info.groupOwnerAddress, hardwareAddress);
                        if(client.connexionEstablished()) {
                            drawActivity.setTransmission(client);
                            drawActivity.setConnected(true);
                            drawActivity.connectionMode = DrawActivity.CLIENT;
                        } else  {
                            drawActivity.setConnected(false);
                        }
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

    /**
     * Create a receiver with the specified manager, channel and activity.
     */
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

            NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            if(networkInfo != null
                &&networkInfo.isConnected())
                wifiP2pManager.requestConnectionInfo(channel, connectionInfoListener);

        } else if(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)){

            if(drawActivity.getAutomaticReconnection())
                wifiP2pManager.requestConnectionInfo(channel,connectionInfoListener);

        } else if(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)){
            WifiP2pDevice device = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
            setHardwareAddress(HardwareAddress.parseHardwareAddress(device.deviceAddress));
        }
    }

    /**
     * Try to connect with the specified P2P device.
     * If the connection fails, an error messgae is sent and a new discovery is started.
     * @param deviceName The device to connect to.
     */
    public void connect(String deviceName){
        if(!drawActivity.isWifiP2pEnabled()
                || peersInfo.get(deviceName) == null) {
            Toast.makeText(drawActivity, "Couldn't initiate connection", Toast.LENGTH_SHORT).show();
            return;
        }

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
                            Toast.makeText(drawActivity, R.string.connection_failed, Toast.LENGTH_SHORT)
                                    .show();
                            wifiP2pManager.discoverPeers(channel,discover);
                        }
                    }
            );
    }

    /**
     * Set the hardware address and notify the draw activity.
     * @param hardwareAddress The new hardware address.
     */
    private void setHardwareAddress(HardwareAddress hardwareAddress) {
        this.hardwareAddress = hardwareAddress;
        drawActivity.hardwareAddressAvailable();
    }
    public ArrayList<String> getPeersName() {
        return new ArrayList<>(peersInfo.keySet());
    }
}
