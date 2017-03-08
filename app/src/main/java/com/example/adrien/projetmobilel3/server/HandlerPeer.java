package com.example.adrien.projetmobilel3.server;

import android.app.NotificationManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.NotificationCompat;
import android.widget.Toast;

import com.example.adrien.projetmobilel3.R;
import com.example.adrien.projetmobilel3.draw.Point;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by MrkJudge on 24/02/2017.
 */

//TODO non terminé + discuter de l'AsyncTask ou Thread
public class HandlerPeer extends Thread {

    private ServerP2P server;
    private Socket socket;
    private OutputStream os;

    private boolean stop = false;
    private int pointPacketLenght = Point.getByteLength();
    private final ArrayList<Point> points = new ArrayList<>();

    public HandlerPeer(ServerP2P server,Socket socket) {
        this.server = server;
        this.socket = socket;
        server.getHandlers().add(this);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(server.getMainActivity());
        builder.setContentTitle("Server created")
                .setContentText("")
                .setSmallIcon(R.drawable.message_received);
        NotificationManager nm = (NotificationManager) server.getMainActivity().getSystemService(NOTIFICATION_SERVICE);
        nm.notify(0,builder.build());
    }

    public OutputStream getOutPutSteam() { return os; }
    public ArrayList<Point> getPoints() { return points; }
    public synchronized ArrayList<Point> gatherPoints() {
        ArrayList<Point> pointsGathered = new ArrayList<>(getPoints());
        getPoints().clear();
        return pointsGathered;
    }

    @Override
    public void run() {
        super.run();

        try {
            os = socket.getOutputStream();
            InputStream buffer = socket.getInputStream();

            while(!stop) {

                byte[] bufferData = new byte[Point.getByteLength()];
                buffer.read(bufferData);
                Point p = new Point(bufferData);
                server.getMainActivity().getDraw().addPoint(p);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(server.getMainActivity());
                builder.setContentTitle("Message received")
                .setContentText("Message received from socket")
                .setSmallIcon(R.drawable.message_received);
                NotificationManager nm = (NotificationManager) server.getMainActivity().getSystemService(NOTIFICATION_SERVICE);
                nm.notify(0,builder.build());

                os.write(new byte[] {1,0});

                //buffer.read(new byte[pointPacketLenght], 0, pointPacketLenght);
                //TODO méthode de déserialization + envoie des données vers l'application

            }
            socket.close();points.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }

        server.getHandlers().remove(this);
    }
}
