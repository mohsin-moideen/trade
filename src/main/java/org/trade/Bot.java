package org.trade;

import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Strategy;
import org.ta4j.core.Trade.TradeType;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import org.trade.core.Strategies;
import org.trade.core.beans.StrategyConfig;
import org.trade.enums.Timeframe;
import org.trade.loaders.DataLoader;
import org.trade.loaders.MetaapiDataLoader;

public class Bot {

	private static final Logger log = LogManager.getLogger(Bot.class);

	public static void main(String[] args) {
		final String SYMBOL_EURUSD = "EURUSD";
		final String SYMBOL_GBPUSD = "GBPUSD";

		final Timeframe timeframe = Timeframe.fifteen_min;
		final Num volume = DecimalNum.valueOf(0.2);
		BarSeries eurusdSeries = initMovingBarSeries(SYMBOL_EURUSD, timeframe, 1000);
		Strategy strategy1 = Strategies.getVwap9EmaBuyStrategy(eurusdSeries);
		Strategy strategy2 = Strategies.getVwap9EmaSellStrategy(eurusdSeries);

		BarSeries gbpusdSeries = initMovingBarSeries(SYMBOL_GBPUSD, timeframe, 1000);
		Strategy strategy3 = Strategies.getVwap9EmaBuyStrategy(eurusdSeries);
		Strategy strategy4 = Strategies.getVwap9EmaSellStrategy(eurusdSeries);

		List<StrategyConfig> strategyConfigs = new LinkedList<>();

		strategyConfigs.add(new StrategyConfig("EURUSD-Vwap9EmaBuy", SYMBOL_EURUSD, timeframe, volume, eurusdSeries,
				strategy1, TradeType.BUY));
		strategyConfigs.add(new StrategyConfig("EURUSD-Vwap9EmaSell", SYMBOL_EURUSD, timeframe, volume, eurusdSeries,
				strategy2, TradeType.SELL));

		strategyConfigs.add(new StrategyConfig("GBPUSD-Vwap9EmaBuy", SYMBOL_GBPUSD, timeframe, volume, gbpusdSeries,
				strategy3, TradeType.BUY));
		strategyConfigs.add(new StrategyConfig("GBPUSD-Vwap9EmaSell", SYMBOL_GBPUSD, timeframe, volume, gbpusdSeries,
				strategy4, TradeType.SELL));

		for (StrategyConfig strategyConfig : strategyConfigs) {
			Thread app = new Thread(
					new App(strategyConfig.getSymbol(), strategyConfig.getTimeframe(), strategyConfig.getVolume(),
							strategyConfig.getSeries(), strategyConfig.getStrategy(), strategyConfig.getTradeType()));
			app.setName(strategyConfig.getName());
			app.start();
		}
	}

	/**
	 * Builds a moving bar series (i.e. keeping only the maxBarCount last bars)
	 *
	 * @param maxBarCount the number of bars to keep in the bar series (at maximum)
	 * @return a moving bar series
	 */
	private static BarSeries initMovingBarSeries(String symbol, Timeframe timeframe, int maxBarCount) {
		DataLoader dataLoader = new MetaapiDataLoader();
		BarSeries series = dataLoader.getSeries(symbol, maxBarCount, timeframe);
		log.info("Initial bar count: " + series.getBarCount());
		// Limiting the number of bars to maxBarCount
		series.setMaximumBarCount(1500);
		Num LAST_BAR_CLOSE_PRICE = series.getBar(series.getEndIndex()).getClosePrice();
		log.info(" (limited to " + maxBarCount + "), close price = " + LAST_BAR_CLOSE_PRICE);
		return series;
	}

}