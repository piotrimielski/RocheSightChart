package com.givevision.rochesightchart;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;

//import com.givevision.sightplus.util.Constants;

public class Battery {
	private SharedPreferences systemSharedData;
	private Context _context;
	private IntentFilter ifilter = new IntentFilter("android.intent.action.BATTERY_CHANGED");

	public Battery(Context context) {
		this._context = context;
//		this.systemSharedData = this._context.getSharedPreferences(Constants.pref_key_name, 0);
	}

	public void updateBatteryLevel(int level) {
//		Constants.battery_level = level;
	}

	public void updateBatteryTempMax(float max) {
//		Constants.battery_max_temp = max;
	}

	public String getCurrentTemperature() {
		Intent batteryTemp = this._context.registerReceiver((BroadcastReceiver)null, this.ifilter);
		float temp = (float)batteryTemp.getIntExtra("temperature", 0) / 10.0F;
		return String.valueOf(temp) + " ℃";
	}

	public String getCurrentLevel() {
		Intent batteryStatus = this._context.registerReceiver((BroadcastReceiver)null, this.ifilter);
		int level = batteryStatus.getIntExtra("level", -1);
		return String.valueOf(level);
	}

	public float getFloatCurrentTemperature() {
		Intent batteryTemp = this._context.registerReceiver((BroadcastReceiver)null, this.ifilter);
		float temp = (float)batteryTemp.getIntExtra("temperature", 0) / 10.0F;
		return temp;
	}

	public int getIntCurrentLevel() {
		Intent batteryStatus = this._context.registerReceiver((BroadcastReceiver)null, this.ifilter);
		int level = batteryStatus.getIntExtra("level", -1);
		return level;
	}

}
