package com.example.adrien.projetmobilel3.common;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.nio.ByteBuffer;
import java.util.Comparator;

/**
 * The HardwareAddress class represents an hardware address of a device.
 */
public class HardwareAddress implements Comparable<HardwareAddress>, Parcelable{

    /**
     * Length in byte of a HardwareAddress.
     */
    public static final int BYTES = 16;

    /**
     * The byte array containing the hardware address.
     */
	private byte[] address = new byte[BYTES];

    /**
     * Create a hardware address with the specified address.
     * @param address The byte array containing the address.
     */
	public HardwareAddress(byte[] address){
		this.address = address;
	}

    /**
     * Create a hardware address from a parcel.
     * @param parcel The parcel containing the hardware address.
     */
	protected HardwareAddress(Parcel parcel) {
		address = parcel.createByteArray();
	}

	public static final Creator<HardwareAddress> CREATOR = new Creator<HardwareAddress>() {
		@Override
		public HardwareAddress createFromParcel(Parcel in) {
			return new HardwareAddress(in);
		}

		@Override
		public HardwareAddress[] newArray(int size) {
			return new HardwareAddress[size];
		}
	};

    /**
     * Return a byte array with hardware address.
     * @return The byte array containing the hardware address.
     */
	public byte[] getBytes(){
		return address;
	}

    /**
     * Create and return a new HardwareAddress object with the specified address.
     * @param address The byte array containing the hardware address.
     * @return The hardware address created.
     */
    public static HardwareAddress parseHardwareAddress(byte [] address){
        HardwareAddress chaddr = new HardwareAddress(address);
        return  chaddr;
    }

    /**
     * Create and return a new HardwareAddress object with the specified address.
     * @param address The byte buffer containing the hardware address.
     * @return The hardware address created.
     */
	public static HardwareAddress parseHardwareAddress(ByteBuffer address){
		byte[] addressBytes = new byte[BYTES];
		address.get(addressBytes);
		HardwareAddress chaddr = new HardwareAddress(addressBytes);
		return  chaddr;
	}

    /**
     * Create and return a new HardwareAddress object with the specified address.
     * @param address The string containing the hardware address.
     * @return The hardware address created.
     */
	public static HardwareAddress parseHardwareAddress(String address){
		byte[] addressBytes = new byte[BYTES];
		
		ByteBuffer bb = ByteBuffer.wrap(address.getBytes());
		
		for(int i = addressBytes.length -1; i > 0 ; i -= 4) {
			addressBytes[i  ] = bb.get();
			addressBytes[i-1] = bb.get();
			
			addressBytes[i-2] = bb.get();
			addressBytes[i-3] = bb.get();
			
			// / ou :bb.getChar();
		}
		
		return new HardwareAddress(addressBytes);
	}

    /**
     * Return a string of the hardware address.
     * @return The string containing the hardware address.
     */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		for(byte b: address)
			sb.append(String.format("%02X ", b));
			
		return sb.toString();
	}

    /**
     * Compare this hardware address with an other object.
     * An other hardware address is expected.
     * Any other object will return instantly false.
     * @param obj The object to test.
     * @return True if the hardware address is equal.
     */
    @Override
    public boolean equals(Object obj) {
        if(obj instanceof HardwareAddress) {
            return this.toString().equals(obj.toString());
        }
        return false;
    }

    /**
     * Compare this hardware address with an other one.
     * @param address The object to compare.
     * @return True if the hardware address is the same one.
     */
    @Override
    public int compareTo(@NonNull HardwareAddress address) {
        if(this.equals(address))
            return 0;
        else
            return 1;
    }

    /**
     * Parcelable interface methods.
     */
	@Override
	public int describeContents() {
		return 0;
	}

    /**
     * Write this hardware address to the specified parcel.
     * @param parcel The parcel to write the hardware address into.
     */
	@Override
	public void writeToParcel(Parcel parcel, int i) {
		parcel.writeByteArray(address);
	}
}
