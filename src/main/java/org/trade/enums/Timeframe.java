package org.trade.enums;

public enum Timeframe {
	one_min("1m"), five_min("5m"), fifteen_min("15m"), thirty_min("30m"), one_hour("1h"), four_hour("4h"),
	twelve_hour("12h"), one_day("1d");

	public final String value;

	private Timeframe(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return this.value;
	}

}
