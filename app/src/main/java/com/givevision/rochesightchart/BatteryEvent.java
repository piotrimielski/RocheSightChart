package com.givevision.rochesightchart;

public class BatteryEvent {
	private final int battery_level;

	public BatteryEvent(int battery_msg) {
		this.battery_level = battery_msg;
	}

	public int getBattery_msg() {
		return this.battery_level;
	}
	

}
