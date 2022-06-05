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

	public static int getMintues(Timeframe timeframe) {
		switch (timeframe) {
		case fifteen_min:
			return 15;
		case five_min:
			return 5;
		case four_hour:
			return 4 * 60;
		case one_day:
			return 24 * 60;
		case one_hour:
			return 60;
		case one_min:
			return 1;
		case thirty_min:
			return 30;
		case twelve_hour:
			return 12 * 60;
		default:
			return 0;
		}
	}

}
