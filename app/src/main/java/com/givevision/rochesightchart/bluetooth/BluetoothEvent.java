package com.givevision.rochesightchart.bluetooth;

import java.util.ArrayList;

public class BluetoothEvent {
//	private final String bluetooth_msg;
	private ArrayList<String> bluetooth_msg;
	public BluetoothEvent(ArrayList<String> bluetooth_msg) {
		this.bluetooth_msg = bluetooth_msg;
	}

	public ArrayList getBluetooth_msg() {
		return this.bluetooth_msg;
	}
}
