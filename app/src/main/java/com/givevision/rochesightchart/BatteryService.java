package com.givevision.rochesightchart;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

//import com.givevision.sightplus.util.Constants;

import org.greenrobot.eventbus.EventBus;

public class BatteryService extends BroadcastReceiver {

	private final static String BATTERY_LEVEL = "level";

	public BatteryService() {
	}

	public void onReceive(Context context, Intent intent) {
		int level = intent.getIntExtra("level", 0);
		float temp = (float)intent.getIntExtra("temperature", 0) / 10.0F;
		int plugged = intent.getIntExtra("plugged", -1);
		boolean isCharging = plugged == 1 || plugged == 2;
//		if (level <= Constants.battery_level && !isCharging) {
//			EventBus.getDefault().post(new com.givevision.methods.battery.BatteryEvent(level));
//		}
//		if (temp >= Constants.battery_max_temp) {
//			EventBus.getDefault().post(new BatteryTemperatureEvent(String.valueOf(temp), String.valueOf(level)));
//		}

	}

}
