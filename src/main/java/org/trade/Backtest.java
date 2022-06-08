package org.trade;

import org.ta4j.core.AnalysisCriterion;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BarSeriesManager;
import org.ta4j.core.Strategy;
import org.ta4j.core.Trade.TradeType;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.analysis.criteria.pnl.ProfitLossCriterion;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import org.trade.core.Strategies;
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

	public static void main(String[] args) {
		BarSeries series = initMovingBarSeries("EURUSD", Timeframe.fifteen_min, 16);
		BarSeriesManager seriesManager = new BarSeriesManager(series);
		Strategy strategy1 = Strategies.getVwap9EmaBuyStrategy(series);
		Strategy strategy2 = Strategies.getVwap9EmaSellStrategy(series);

		TradingRecord tradingRecord1 = seriesManager.run(strategy1, TradeType.BUY, DecimalNum.valueOf(10000));

		TradingRecord tradingRecord2 = seriesManager.run(strategy2, TradeType.SELL, DecimalNum.valueOf(10000));

		AnalysisCriterion criterion = new ProfitLossCriterion();
		System.out.println("strategy 1 -> " + (criterion.calculate(series, tradingRecord1).doubleValue()));
		System.out.println("strategy 2 -> " + (criterion.calculate(series, tradingRecord2).doubleValue()));
	}
}
