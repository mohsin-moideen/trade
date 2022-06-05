package org.trade;

import org.ta4j.core.AnalysisCriterion;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BarSeriesManager;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;
import org.ta4j.core.Trade.TradeType;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.analysis.criteria.pnl.GrossProfitCriterion;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.StopGainRule;
import org.ta4j.core.rules.StopLossRule;
import org.ta4j.core.rules.UnderIndicatorRule;
import org.trade.enums.Timeframe;
import org.trade.loaders.DataLoader;
import org.trade.loaders.MetaapiDataLoader;

import ta4jexamples.strategies.UnstableIndicatorStrategy;

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
		// Getting the close price of the bars
		Num firstClosePrice = series.getBar(0).getClosePrice();
		System.out.println("First close price: " + firstClosePrice.doubleValue());
		// Or within an indicator:
		ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
		// Here is the same close price:
		System.out.println(firstClosePrice.isEqual(closePrice.getValue(0))); // equal to firstClosePrice

		// Getting the simple moving average (SMA) of the close price over the last 5
		// bars
		SMAIndicator shortSma = new SMAIndicator(closePrice, 5);
		// Here is the 5-bars-SMA value at the 42nd index
		System.out.println("5-bars-SMA value at the 42nd index: " + shortSma.getValue(42).doubleValue());

		// Getting a longer SMA (e.g. over the 30 last bars)
		SMAIndicator longSma = new SMAIndicator(closePrice, 30);

		Rule buyingRule = new CrossedUpIndicatorRule(shortSma, longSma)
				.or(new CrossedDownIndicatorRule(closePrice, 800));

		// Selling rules
		// We want to sell:
		// - if the 5-bars SMA crosses under 30-bars SMA
		// - or if the price loses more than 3%
		// - or if the price earns more than 2%
		Rule sellingRule = new CrossedDownIndicatorRule(shortSma, longSma)
				.or(new StopLossRule(closePrice, series.numOf(3))).or(new StopGainRule(closePrice, series.numOf(2)));

		return new BaseStrategy(buyingRule, sellingRule);
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
		BarSeriesManager seriesManager = new BarSeriesManager(series);
		Strategy strategy1 = buildSMAStrategy2(series);
		TradingRecord tradingRecord1 = seriesManager.run(strategy1, TradeType.BUY, DecimalNum.valueOf(10000));

		Strategy strategy2 = UnstableIndicatorStrategy.buildStrategy(series);
		TradingRecord tradingRecord2 = seriesManager.run(strategy2, TradeType.BUY, DecimalNum.valueOf(10000));

		AnalysisCriterion criterion = new GrossProfitCriterion();
		System.out.println("strategy 1 " + (criterion.calculate(series, tradingRecord1).doubleValue()));
		System.out.println("strategy 2 " + (criterion.calculate(series, tradingRecord2).doubleValue()));
	}
}
