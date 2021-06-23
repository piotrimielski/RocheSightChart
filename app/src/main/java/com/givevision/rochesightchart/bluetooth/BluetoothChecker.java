package com.givevision.rochesightchart.bluetooth;

import android.bluetooth.BluetoothAdapter;

public class BluetoothChecker {
	private static BluetoothAdapter mBluetoothAdapter;

	public BluetoothChecker() {
	}

	public static BluetoothAdapter enableBluetooth() {
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (!mBluetoothAdapter.isEnabled()) {
			mBluetoothAdapter.enable();
			if(mBluetoothAdapter.isEnabled()){
                return mBluetoothAdapter;
            }
		}
		return mBluetoothAdapter;

	}
}
