package com.example.adrien.projetmobilel3.common;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.nio.ByteBuffer;
import java.util.Comparator;

public class HardwareAddress implements Comparable<HardwareAddress>, Parcelable{

    public static final int BYTES = 16;

	private byte[] address = new byte[BYTES];
	
	public HardwareAddress(byte[] address){
		this.address = address;
	}

	protected HardwareAddress(Parcel in) {
		address = in.createByteArray();
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

	public byte[] getBytes(){
		return address;
	}

    public static HardwareAddress parseHardwareAddress(byte [] bytes){
        HardwareAddress chaddr = new HardwareAddress(bytes);
        return  chaddr;
    }

	public static HardwareAddress parseHardwareAddress(ByteBuffer buffer){
		byte[] address = new byte[BYTES];
		buffer.get(address);
		HardwareAddress chaddr = new HardwareAddress(address);
		return  chaddr;
	}
	
	public static HardwareAddress parseHardwareAddress(String buffer){
		byte[] address = new byte[BYTES];
		
		ByteBuffer bb = ByteBuffer.wrap(buffer.getBytes());
		
		for(int i = address.length -1; i > 0 ; i -= 4) {
			address[i  ] = bb.get();
			address[i-1] = bb.get();
			
			address[i-2] = bb.get();
			address[i-3] = bb.get();
			
			// / ou :bb.getChar();
		}
		
		return new HardwareAddress(address);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		for(byte b: address)
			sb.append(String.format("%02X ", b));
			
		return sb.toString();
	}

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof HardwareAddress) {
            return this.toString().equals(obj.toString());
        }
        return false;
    }

    @Override
    public int compareTo(@NonNull HardwareAddress o) {
        if(this.equals(o))
            return 0;
        else
            return 1;
    }

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel parcel, int i) {
		parcel.writeByteArray(address);
	}
}
