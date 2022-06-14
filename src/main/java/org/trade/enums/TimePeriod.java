package org.trade.enums;

public enum TimePeriod {
	one_day, one_week, one_month, three_months, six_months, one_year;

	public static int getMintues(TimePeriod timePeriod) {
		final int minutesInADay = 60 * 24;
		switch (timePeriod) {
		case one_day:
			return minutesInADay;
		case one_month:
			return 30 * minutesInADay;
		case one_week:
			return 70 * minutesInADay;
		case one_year:
			return 365 * minutesInADay;
		case six_months:
			return 180 * minutesInADay;
		case three_months:
			return 90 * minutesInADay;
		default:
			return 0;

		}
	}
}
