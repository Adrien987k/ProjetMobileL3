package com.example.adrien.projetmobilel3.common;

import java.nio.ByteBuffer;

/**
 * The Message class regroups required information at initialization with a server.
 * Message are created the sent just after the connection with the server to identify
 * itself with his hardware address.
 */
public class Message {

    /**
     * Length iu byte of a Message
     */
    public static final int BYTES = HardwareAddress.BYTES;

    /**
     * The hardware address to deliver.
     */
    private HardwareAddress hardwareAddress;

    /**
     * Create a message with the specified hardware address into.
     */
    public Message(HardwareAddress hardwareAddress) {
        this.hardwareAddress = hardwareAddress;
    }

    /**
     * Create a message with the specified hardware address into.
     * @param bytes The byte array containing the hardware address.
     */
    public Message(byte[] bytes) {
        this.hardwareAddress = HardwareAddress.parseHardwareAddress(bytes);
    }

    public HardwareAddress getHardwareAddress() {
        return hardwareAddress;
    }

    /**
     * Return a byte array with all information of the message
     * @return The byte array containing all information of the message.
     */
    public byte[] getBytes() {
        return hardwareAddress.getBytes();
    }

}
