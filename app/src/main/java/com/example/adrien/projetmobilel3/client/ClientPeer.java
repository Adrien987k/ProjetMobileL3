package com.example.adrien.projetmobilel3.client;

import android.content.Context;
import android.net.ConnectivityManager;

import com.example.adrien.projetmobilel3.activities.DrawActivity;
import com.example.adrien.projetmobilel3.common.DrawTools;
import com.example.adrien.projetmobilel3.common.HardwareAddress;
import com.example.adrien.projetmobilel3.common.Message;
import com.example.adrien.projetmobilel3.common.PointPacket;
import com.example.adrien.projetmobilel3.common.PointTransmission;
import com.example.adrien.projetmobilel3.draw.Draw;
import com.example.adrien.projetmobilel3.server.ServerP2P;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.TreeMap;

/**
 * The ClientPeer class is used to connect to a server.
 * This client sends and receives data.
 * At initialization, the client send a message with
 * the hardware address ot the device, then wait
 * for data to write or read.
 */

public class ClientPeer extends Thread implements PointTransmission {

    /**
     * The socket to write and read data.
     */
    private Socket socket;

    /**
     * The output stream from the socket.
     */
    private OutputStream os;

    /**
     * The link to the draw activity.
     */
    private DrawActivity drawActivity;

    /**
     * The hardware address of the device.
     */
    private HardwareAddress hardwareAddress;

    /**
     * The IP address of the server.
     */
    private InetAddress serverAddress;

    /**
     * Indicate if the client must stop.
     */
    private boolean stop = false;

    /**
     * Indicate if the connection to the server is established.
     */
    private boolean connexionEstablished = false;

    /**
     * Indicate if the client has already attempted to connect to the server.
     */
    private boolean connexionAttempt = false;

    /**
     * Create a client with the specified draw activity, server address and device hardware address.
     * @param drawActivity The link to the draw activity.
     * @param serverAddress The IP address of the server.
     * @param hardwareAddress The hardware address of the device.
     */
    public ClientPeer(DrawActivity drawActivity, InetAddress serverAddress, HardwareAddress hardwareAddress) {
        this.drawActivity = drawActivity;
        this.serverAddress = serverAddress;
        this.hardwareAddress = hardwareAddress;
        start();
    }

    @Override
    public void run() {
        super.run();
        ConnectivityManager cm = (ConnectivityManager) (drawActivity.getSystemService(Context.CONNECTIVITY_SERVICE));
        if(cm != null
                && cm.getActiveNetworkInfo().isConnected())  {

            socket = null;
            try {
                socket = new Socket(serverAddress, ServerP2P.DEFAULT_PORT);
                connexionEstablished = true;
                this.os = socket.getOutputStream();
                os.write(new Message(hardwareAddress).getBytes());
                System.out.println("Socket created, client side");
                connexionAttempt = true;
                //System.out.println(hardwareAddress);
            } catch (IOException e) {
                connexionAttempt = true;
                setStop(true);
                e.printStackTrace();
                return;
            }

        try {
            InputStream buffer = socket.getInputStream();
            byte[] bufferData;
            while(!stop) {
                bufferData = new byte[PointPacket.BYTES];
                buffer.read(bufferData);
                handleData(new PointPacket(bufferData));
            }
        } catch (SocketException e) {
            e.printStackTrace();
            setStop(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
            System.out.println("Socket Closed, client side");
        }
    }


    /**
     * Handle a point packet received from the server.
     * The point is identified by the hardware address of the drawer.
     * @param pointPacket
     */
    private synchronized void handleData(PointPacket pointPacket) {
        HardwareAddress hardwareAddressReceived = pointPacket.getHardwareAddress();
        getDrawActivity().getDraw().getPoints().add(pointPacket);
        if(!hardwareAddressReceived.equals(hardwareAddress)) {
            if (getUsers().containsKey(hardwareAddressReceived)) {
                getDraw().drawPointPacket(getUsers().get(hardwareAddressReceived), pointPacket);
            } else {
                getUsers().put(new HardwareAddress(hardwareAddressReceived.getBytes()), new DrawTools());
                getDraw().drawPointPacket(getUsers().get(hardwareAddressReceived), pointPacket);
            }
        }
    }

    private DrawActivity getDrawActivity() {
        return drawActivity;
    }
    private Draw getDraw() {
        return getDrawActivity().getDraw();
    }
    private HardwareAddress getHardwareAddress() {
        return getDrawActivity().getHardwareAddress();
    }
    private TreeMap<HardwareAddress,DrawTools> getUsers() {
        return getDrawActivity().getUsers();
    }

    /**
     * Set the client state.
     * @param stop True if the client must stop.
     */
    @Override
    public void setStop(boolean stop) {
        this.stop = stop;
        connexionEstablished = false;
    }

    /**
     * Send a point packet to the server.
     * @param pointPacket The point packet to send.
     */
    @Override
    public synchronized void addPointPacket(PointPacket pointPacket) {
        try {
            os.write(pointPacket.getBytes());
        } catch (SocketException e) {
            e.printStackTrace();
            setStop(true);
        } catch (IOException e) {
            e.printStackTrace();
            setStop(true);
        } catch (NullPointerException e) {
            e.printStackTrace();
            setStop(true);
        }
    }

    /**
     * Indicate if the connection is established.
     * This method will block until an effective attempt is done.
     * @return True if the connection to the server is established.
     */
    public boolean connexionEstablished() {
        while (!connexionAttempt) {}
        return connexionEstablished;
    }


}

