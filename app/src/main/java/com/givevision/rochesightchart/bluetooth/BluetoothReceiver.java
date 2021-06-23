package com.givevision.rochesightchart.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

public class BluetoothReceiver extends BroadcastReceiver {
	public BluetoothReceiver() {
	}

	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		BluetoothDevice device= intent.getParcelableExtra (BluetoothDevice.EXTRA_DEVICE);
		ArrayList<String> bluetoothMsg = new ArrayList<>();
		bluetoothMsg.add(device.getName());

		switch(action.hashCode()) {
			case -301431627:
				if (action.equals("android.bluetooth.device.action.ACL_CONNECTED")) {
					bluetoothMsg.add("connected");
					EventBus.getDefault().post(new BluetoothEvent(bluetoothMsg));
				}
				break;
//			case -223687943:
//				if (action.equals("android.bluetooth.device.action.PAIRING_REQUEST")) {
//					bluetoothMsg.add("pairingRequest");
//					EventBus.getDefault().post(new BluetoothEvent(bluetoothMsg));
//				}
//				break;
			case 1821585647:
				if (action.equals("android.bluetooth.device.action.ACL_DISCONNECTED")) {
					bluetoothMsg.add("disconnected");
					EventBus.getDefault().post(new BluetoothEvent(bluetoothMsg));
				}
		}

	}

}
