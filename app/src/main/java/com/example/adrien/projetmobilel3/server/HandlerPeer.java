package com.example.adrien.projetmobilel3.server;

import android.graphics.Paint;
import android.graphics.Path;
import android.view.MotionEvent;

import com.example.adrien.projetmobilel3.activities.DrawActivity;
import com.example.adrien.projetmobilel3.common.DrawTools;
import com.example.adrien.projetmobilel3.common.HardwareAddress;
import com.example.adrien.projetmobilel3.common.Message;
import com.example.adrien.projetmobilel3.common.PointPacket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

/**
 * The HandlerPeer class is created when a client try to connect to the server.
 * The handler will wait for a init message from the client to register them
 * into the server.
 */

public class HandlerPeer extends Thread {

    private ServerP2P server;
    private Socket socket;
    private HardwareAddress hardwareAddress;

    /**
     * The output stream of the socket
     */
    private OutputStream os;

    /**
     * Indicate if the handler must stop.
     */
    private boolean stop = false;

    /**
     * Draw tools of the client.
     * Check DrawTools documentation for more.
     */
    private DrawTools drawTools;

    /**
     * The path where the user draw.
     */
    private Path localPath = new Path();

    /**
     * The paint used to draw the path.
     */
    private Paint localPaint = new Paint();


    /**
     * An array list where each point received from the client is stored.
     * Note that this list is frequently gathered and reset.
     */
    private final ArrayList<PointPacket> pointPackets = new ArrayList<>();

    /**
     * Create a new handler bound to the specified server using the specified socket.
     * @param server The server bound to.
     * @param socket The socket to use.
     */
    public HandlerPeer(ServerP2P server,Socket socket) {
        this.server = server;
        this.socket = socket;
        init();
    }

    /**
     * Initialize the paint of the client.
     */
    private void init() {
        localPaint.setAntiAlias(true);
        localPaint.setStyle(Paint.Style.STROKE);
        localPaint.setStrokeJoin(Paint.Join.ROUND);
        localPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    public DrawActivity getMainActivity() {
        return server.getDrawActivity();
    }
    public ArrayList<PointPacket> getPointPackets() {
        return pointPackets;
    }

    /**
     * This method is called by the synchronizer to get points from the points list.
     * The list is immediately reset.
     * @return The array list containing client's points
     */
    public synchronized ArrayList<PointPacket> gatherPoints() {
        ArrayList<PointPacket> knownPointPackets = new ArrayList<>(pointPackets);
        pointPackets.clear();
        return knownPointPackets;
    }

    @Override
    public void run() {
        super.run();
        System.out.println("Handler created");
        try {
            os = socket.getOutputStream();
            InputStream buffer = socket.getInputStream();
            byte[] bufferData;

            bufferData = new byte[HardwareAddress.BYTES];

            buffer.read(bufferData);
            initMessage(new Message(bufferData));

            while (!stop) {
                bufferData = new byte[PointPacket.BYTES];
                buffer.read(bufferData);
                handleData(new PointPacket(bufferData));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            server.getHandlers().remove(hardwareAddress);
            System.out.println("Handler unregistered. nb: " + server.getHandlers().size());
            System.out.println("Handler Closed");
        }

    }

    /**
     * Register the client in the server with his hardware address.
     * @param message The init message containing the hardware address.
     */
    public void initMessage(Message message) {
        hardwareAddress = message.getHardwareAddress();
        server.getHandlers().put(hardwareAddress,this);
        System.out.println("Handler registered. nb: " + server.getHandlers().size());
        sendPointPackets(server.getSynchronizer().getPointPacketsFromStart());
    }

    /**
     * Send an array list of point packets to the client
     * @param pointPackets The array list of points packets to send.
     */
    public synchronized void sendPointPackets(ArrayList<PointPacket> pointPackets) {
        try {
            for(PointPacket pointPacket: pointPackets) {
                os.write(pointPacket.getBytes());
            }
        } catch (SocketException e) {
            e.printStackTrace();
            setStop(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Called after reading a point packet.
     * This will draw the point on the server draw and add it to the point list.
     * @param pointPacket The point packet received.
     */
    private void handleData(PointPacket pointPacket) {

        pointPackets.add(pointPacket);

        float x = pointPacket.getPoint().getX();
        float y = pointPacket.getPoint().getY();

        switch (pointPacket.getAction()) {
            case MotionEvent.ACTION_DOWN:
                localPaint.setStrokeWidth(pointPacket.getPoint().getStroke());
                localPaint.setColor(pointPacket.getPoint().getColor());
                localPath.moveTo(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                myLineTo(x, y);
                break;
            case MotionEvent.ACTION_UP:
                myLineTo(x, y);
                localPath.reset();
                break;
        }
    }

    /**
     * A personal implementation of the method Path.lineTo.
     * This method must be used instead of Path.lineTo if you have multiple users.
     * It will prevent you to have superposition conflicts,
     * because only the new point, and therefore the line to the last point, is drawn.
     */
    private synchronized void myLineTo(float x, float y) {
        localPath.lineTo(x, y);
        getMainActivity().getDraw().getMyCanvas().drawPath(localPath, localPaint);
        getMainActivity().getDraw().postInvalidate();
        localPath.reset();
        localPath.moveTo(x,y);
    }

    /**
     * Set the handler state.
     * @param stop True if the handler must stop.
     */
    public void setStop(boolean stop) {
        this.stop = stop;
    }
}
