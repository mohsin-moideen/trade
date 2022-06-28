package org.trade.utils;

import java.util.Queue;

import org.apache.commons.collections4.queue.CircularFifoQueue;

public class TrendAnalysis {

	private Double high, low, recentHigh, recentLow, recentPrice;
	private boolean isBullish;
	private boolean isTrendReversed;
	private Integer trendStreak;

	public boolean isTrendReversed() {
		return isTrendReversed;
	}

	public Boolean isBullish() {
		return isBullish;
	}

	public void updateTrend(Double recentPrice) {
		this.recentPrice = recentPrice;
		if (high == null) {
			init();
			return;
		}
		isTrendReversed = false;
		System.out.println("recentPrice = " + recentPrice);
		if (recentPrice > recentHigh) {
			recentHigh = recentPrice;
			System.out.println("recent high set");

		} else if (recentPrice < recentLow) {
			recentLow = recentPrice;
			System.out.println("recent low set");

		}
		if (isBullish) {
			System.out.println("is bullish");

			if (recentHigh > high) {
				System.out.println("setting higher high");

				high = recentHigh;
				low = recentLow;
				recentLow = recentPrice;
				trendStreak++;

			} else if (recentLow < low) {
				System.out.println("setting lower low");

				setTrendReversed(PriceMovement.LowerLow);
				low = recentLow;
				isBullish = false;
				high = recentHigh;
				recentLow = recentPrice;
				trendStreak = 1;

			}
		} else {
			System.out.println("is not bullish");

			if (recentHigh > high) {
				System.out.println("setting higher high");

				setTrendReversed(PriceMovement.HigherHigh);
				high = recentHigh;
				low = recentLow;
				recentHigh = recentPrice;
				isBullish = true;
				trendStreak = 1;
			} else if (recentLow < low) {
				System.out.println("setting lower low");
				low = recentLow;
				high = recentHigh;
				recentHigh = recentPrice;
				trendStreak++;
			}
		}
		print();
	}

	private void init() {
		high = recentPrice;
		recentHigh = recentPrice;
		low = recentPrice;
		recentLow = recentPrice;
	}

	public Double getTrendStrength() {
		if (isBullish) {
			return Math.abs(low - recentPrice) / recentPrice * 100 * trendStreak;
		} else {
			return Math.abs(high - recentPrice) / recentPrice * 100 * trendStreak;

		}
	}

	private void print() {
		System.out.println(this);
		System.out.println("getTrendStrength() " + getTrendStrength());
	}

	@Override
	public String toString() {
		return "TrendAnalysis high=" + high + ", low=" + low + ", recentHigh=" + recentHigh + ", recentLow=" + recentLow
				+ ", isBullish=" + isBullish + ", isTrendReversed=" + isTrendReversed + "]";
	}

	private void setTrendReversed(PriceMovement movement) {
		if ((!isBullish && movement == PriceMovement.HigherHigh) || isBullish && movement == PriceMovement.LowerLow) {
			System.out.println("isTrendReversed true");

			isTrendReversed = true;
		} else {
			System.out.println("isTrendReversed false");

			isTrendReversed = false;
		}
	}

	public static void main(String[] args) {

		Queue<Double> prices = new CircularFifoQueue<Double>(12);
//		prices.add(1.0512);
//		prices.add(0.0522);
//		prices.add(1.0502);
//		prices.add(0.0522);
//		prices.add(1.0502);
//		prices.add(1.0498);
//		prices.add(1.0488);
//		prices.add(1.0498);
//		System.out.println(isBullish());
		prices.add(1.0);
		System.out.println(prices.size());
	}

}

enum PriceMovement {
	HigherHigh, LowerLow
}
