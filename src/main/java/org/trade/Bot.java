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
import org.trade.utils.JsonUtils;

public class Bot {

	private static final Logger log = LogManager.getLogger(Bot.class);

	public static void main(String[] args) {
		final String SYMBOL_EURUSD = "EURUSD";
		final String SYMBOL_GBPUSD = "GBPUSD";

		final Timeframe timeframe = Timeframe.fifteen_min;
		final Num volume = DecimalNum.valueOf(0.2);
		BarSeries eurusdSeries1 = initMovingBarSeries(SYMBOL_EURUSD, timeframe, 500);
		BarSeries eurusdSeries2 = initMovingBarSeries(SYMBOL_EURUSD, timeframe, 500);

		Strategy strategy1 = Strategies.getVwap9EmaBuyStrategy(eurusdSeries1);
		Strategy strategy2 = Strategies.getVwap9EmaSellStrategy(eurusdSeries2);

		BarSeries gbpusdSeries1 = initMovingBarSeries(SYMBOL_GBPUSD, timeframe, 500);
		BarSeries gbpusdSeries2 = initMovingBarSeries(SYMBOL_GBPUSD, timeframe, 500);

		Strategy strategy3 = Strategies.getVwap9EmaBuyStrategy(gbpusdSeries1);
		Strategy strategy4 = Strategies.getVwap9EmaSellStrategy(gbpusdSeries2);

		List<StrategyConfig> strategyConfigs = new LinkedList<>();

		strategyConfigs.add(new StrategyConfig("EURUSD-Vwap9EmaBuy", SYMBOL_EURUSD, timeframe, volume, eurusdSeries1,
				strategy1, TradeType.BUY));
		strategyConfigs.add(new StrategyConfig("EURUSD-Vwap9EmaSell", SYMBOL_EURUSD, timeframe, volume, eurusdSeries2,
				strategy2, TradeType.SELL));

		strategyConfigs.add(new StrategyConfig("GBPUSD-Vwap9EmaBuy", SYMBOL_GBPUSD, timeframe, volume, gbpusdSeries1,
				strategy3, TradeType.BUY));
		strategyConfigs.add(new StrategyConfig("GBPUSD-Vwap9EmaSell", SYMBOL_GBPUSD, timeframe, volume, gbpusdSeries2,
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
		series.setMaximumBarCount(1000);
		Num LAST_BAR_CLOSE_PRICE = series.getBar(series.getEndIndex()).getClosePrice();
		log.info(" (limited to " + maxBarCount + "), close price = " + LAST_BAR_CLOSE_PRICE);
		log.info("candle added to series");
		log.info("series length " + series.getBarCount());
		log.info("series last index " + series.getEndIndex());
		log.info("series last bar " + JsonUtils.getString(series.getBar(series.getEndIndex())));
		return series;
	}

}
