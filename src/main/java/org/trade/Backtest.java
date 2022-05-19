package org.trade;

import org.ta4j.core.AnalysisCriterion;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BarSeriesManager;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Strategy;
import org.ta4j.core.Trade.TradeType;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.analysis.criteria.pnl.GrossProfitCriterion;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.UnderIndicatorRule;
import org.trade.enums.Timeframe;
import org.trade.loaders.DataLoader;
import org.trade.loaders.MetaapiDataLoader;

public class Backtest {

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
	private static Strategy buildSMAStrategy(BarSeries series) {
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

	private static Strategy buildSMAStrategy2(BarSeries series) {
		if (series == null) {
			throw new IllegalArgumentException("Series cannot be null");
		}

		ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
		SMAIndicator sma = new SMAIndicator(closePrice, 2);

		// Signals
		// Buy when SMA goes over close price
		// Sell when close price goes over SMA
		Strategy buySellSignals = new BaseStrategy(new OverIndicatorRule(sma, closePrice),
				new UnderIndicatorRule(sma, closePrice));
		return buySellSignals;
	}

	public static void main(String[] args) {
		BarSeries series = initMovingBarSeries("EURUSD", Timeframe.one_min, 1000);
		BarSeriesManager seriesManager = new BarSeriesManager();
		Strategy strategy1 = buildSMAStrategy(series);
		TradingRecord tradingRecord1 = seriesManager.run(strategy1, TradeType.BUY, DecimalNum.valueOf(0.1));

		Strategy strategy2 = buildSMAStrategy2(series);
		TradingRecord tradingRecord2 = seriesManager.run(strategy2, TradeType.BUY, DecimalNum.valueOf(0.1));

		AnalysisCriterion criterion = new GrossProfitCriterion();
		System.out.println("strategy 1 profit " + criterion.calculate(series, tradingRecord1)); // Returns the result
																								// for
																								// strategy1
		System.out.println("strategy 2 profit " + criterion.calculate(series, tradingRecord2)); // Returns the result
																								// for
																								// strategy2
	}
}
