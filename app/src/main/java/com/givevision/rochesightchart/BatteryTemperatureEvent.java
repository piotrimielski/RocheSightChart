package com.givevision.rochesightchart;

public class BatteryTemperatureEvent {
	private final String battery_temperature_msg;
	private final String battery_msg;

	public BatteryTemperatureEvent(String msg, String msg2) {
		this.battery_temperature_msg = msg;
		this.battery_msg = msg2;
	}

	public String getBattery_temperature_msg() {
		return this.battery_temperature_msg + " ℃";
	}

	public String getBattery_msg() {
		return this.battery_msg + " %";
	}
	

}
