package com.example.adrien.projetmobilel3.common;

/**
 * The PointTransmission interface provides methods to send point packets through the network
 * and to stop network connexion.
 * Handlers serve-side and clients client-side should implements this interface.
 */
public interface PointTransmission {

    /**
     * Send the point packet through the network.
     * Point packets are not necessary sent immediately after the call of this method.
     * @param pointPacket The point packet to send.
     */
    void addPointPacket(PointPacket pointPacket);

    /**
     * Stop the connexion and close the handler or client.
     * This call should close properly the handler or the client
     * and stop the connexion.
     * @param stop
     */
    void setStop(boolean stop);
}
