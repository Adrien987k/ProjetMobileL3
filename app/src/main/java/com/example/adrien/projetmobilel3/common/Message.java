package com.example.adrien.projetmobilel3.common;

import java.nio.ByteBuffer;

/**
 * Created by MrkJudge on 13/03/2017.
 */

public class Message {

    public static final int BYTES = HardwareAddress.BYTES;

    private HardwareAddress hardwareAddress;

    public Message(HardwareAddress hardwareAddress) {
        this.hardwareAddress = hardwareAddress;
    }

    public Message(byte[] bytes) {
        this.hardwareAddress = HardwareAddress.parseHardwareAddress(bytes);
    }

    public HardwareAddress getHardwareAddress() {
        return hardwareAddress;
    }

    public byte[] getBytes() {
        return hardwareAddress.getBytes();
    }

}
