package org.trade;

import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.BaseTradingRecord;
import org.ta4j.core.Strategy;
import org.ta4j.core.Trade;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.UnderIndicatorRule;
import org.trade.beans.Candle;
import org.trade.enums.Timeframe;
import org.trade.loaders.DataLoader;
import org.trade.loaders.MetaapiDataLoader;
import org.trade.utils.meta_api.MarketData;

/**
 * This class is an example of a dummy trading bot using ta4j.
 * <p/>
 */
public class App {

	/**
	 * Close price of the last bar
	 */
	private static Num LAST_BAR_CLOSE_PRICE;

	/**
	 * Builds a moving bar series (i.e. keeping only the maxBarCount last bars)
	 *
	 * @param maxBarCount the number of bars to keep in the bar series (at maximum)
	 * @return a moving bar series
	 */
	private static BarSeries initMovingBarSeries(String symbol, Timeframe timeframe, int maxBarCount) {
		DataLoader dataLoader = new MetaapiDataLoader();
		BarSeries series = dataLoader.getSeries(symbol, maxBarCount, timeframe);
		System.out.print("Initial bar count: " + series.getBarCount());
		// Limitating the number of bars to maxBarCount
		series.setMaximumBarCount(maxBarCount);
		LAST_BAR_CLOSE_PRICE = series.getBar(series.getEndIndex()).getClosePrice();
		System.out.println(" (limited to " + maxBarCount + "), close price = " + LAST_BAR_CLOSE_PRICE);
		return series;
	}

	/**
	 * @param series a bar series
	 * @return a dummy strategy
	 */
	private static Strategy buildStrategy(BarSeries series) {
		if (series == null) {
			throw new IllegalArgumentException("Series cannot be null");
		}

		ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
		SMAIndicator sma = new SMAIndicator(closePrice, 12);

		// Signals
		// Buy when SMA goes over close price
		// Sell when close price goes over SMA
		Strategy buySellSignals = new BaseStrategy(new OverIndicatorRule(sma, closePrice),
				new UnderIndicatorRule(sma, closePrice));
		return buySellSignals;
	}

	private static void updateSeries(BarSeries series, String symbol, Timeframe timeframe) {
		Candle candle = MarketData.getCurrentCandle(symbol, timeframe);
		series.addBar(candle.getZonedDate(), candle.getOpen(), candle.getHigh(), candle.getLow(), candle.getClose(),
				candle.getTickVolume());
	}

	public static void main(String[] args) throws InterruptedException {
		final String SYMBOL = "EURUSD";
		final Timeframe timeframe = Timeframe.one_min;
		System.out.println("********************** Initialization **********************");
		// Getting the bar series
		BarSeries series = initMovingBarSeries(SYMBOL, timeframe, 100);

		// Building the trading strategy
		Strategy strategy = buildStrategy(series);

		// Initializing the trading history
		TradingRecord tradingRecord = new BaseTradingRecord();
		System.out.println("************************************************************");

		for (int i = 0; i < 15; i++) {
			Thread.sleep(60000);
			updateSeries(series, SYMBOL, timeframe);
			Num lastClosePrice = series.getLastBar().getClosePrice();
			System.out.println("------------------------------------------------------\n" + "Bar " + i
					+ " added, close price = " + lastClosePrice);
			int endIndex = series.getEndIndex();
			if (strategy.shouldEnter(endIndex)) {
				// Our strategy should enter
				System.out.println("Strategy should ENTER on " + endIndex);
				boolean entered = tradingRecord.enter(endIndex, lastClosePrice, DecimalNum.valueOf(10));
				if (entered) {
					Trade entry = tradingRecord.getLastEntry();
					System.out.println("Entered on " + entry.getIndex() + " (price=" + entry.getNetPrice().doubleValue()
							+ ", amount=" + entry.getAmount().doubleValue() + ")");
				}
			} else if (strategy.shouldExit(endIndex)) {
				// Our strategy should exit
				System.out.println("Strategy should EXIT on " + endIndex);
				boolean exited = tradingRecord.exit(endIndex, lastClosePrice, DecimalNum.valueOf(10));
				if (exited) {
					Trade exit = tradingRecord.getLastExit();
					System.out.println("Exited on " + exit.getIndex() + " (price=" + exit.getNetPrice().doubleValue()
							+ ", amount=" + exit.getAmount().doubleValue() + ")");
				}
			}
		}
	}
}
