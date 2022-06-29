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
import org.trade.loaders.SeriesUtil;
import org.trade.utils.TelegramUtils;

public class Bot {

	private static final Logger log = LogManager.getLogger(Bot.class);

	public static void main(String[] args) {
		TelegramUtils.isDisabled = false;
		final String SYMBOL_EURUSD = "EURUSD";
		final String SYMBOL_GBPUSD = "GBPUSD";

		final Timeframe timeframe = Timeframe.five_min;
		final Num volume = DecimalNum.valueOf(0.2);

//		BarSeries eurusdSeries1 = SeriesUtil.initMovingBarSeries(SYMBOL_EURUSD, timeframe, 500);
//		BarSeries eurusdSeries2 = SeriesUtil.initMovingBarSeries(SYMBOL_EURUSD, timeframe, 500);
//		Strategy strategy1 = Strategies.getVwap9EmaSellStrategy(eurusdSeries1, DecimalNum.valueOf(0.2),
//				DecimalNum.valueOf(0.09));
//		Strategy strategy2 = Strategies.getVwap9EmaBuyStrategy(eurusdSeries2, DecimalNum.valueOf(0.2),
//				DecimalNum.valueOf(0.09));
//
//		BarSeries gbpusdSeries1 = SeriesUtil.initMovingBarSeries(SYMBOL_GBPUSD, timeframe, 500);
		BarSeries gbpusdSeries2 = SeriesUtil.initMovingBarSeries(SYMBOL_GBPUSD, timeframe, 500);
//		Strategy strategy3 = Strategies.getVwap9EmaSellStrategy(gbpusdSeries1, DecimalNum.valueOf(0.2),
//				DecimalNum.valueOf(0.09));
		Strategy strategy4 = Strategies.getVwap9EmaBuyStrategy(gbpusdSeries2, DecimalNum.valueOf(0.07),
				DecimalNum.valueOf(0.09));

		List<StrategyConfig> strategyConfigs = new LinkedList<>();

//		strategyConfigs.add(new StrategyConfig("EURUSD-Vwap9EmaSell", SYMBOL_EURUSD, timeframe, volume, eurusdSeries1,
//				strategy1, TradeType.SELL));
//		strategyConfigs.add(new StrategyConfig("EURUSD-Vwap9EmaBuy", SYMBOL_EURUSD, timeframe, volume, eurusdSeries2,
//				strategy2, TradeType.BUY));
//
//		strategyConfigs.add(new StrategyConfig("GBPUSD-Vwap9EmaSell", SYMBOL_GBPUSD, timeframe, volume, gbpusdSeries1,
//				strategy3, TradeType.SELL));
		strategyConfigs.add(new StrategyConfig("GBPUSD-Vwap9EmaSell", SYMBOL_GBPUSD, timeframe, volume, gbpusdSeries2,
				strategy4, TradeType.SELL));

		for (StrategyConfig strategyConfig : strategyConfigs) {
			Thread app = new Thread(
					new App(strategyConfig.getSymbol(), strategyConfig.getTimeframe(), strategyConfig.getVolume(),
							strategyConfig.getSeries(), strategyConfig.getStrategy(), strategyConfig.getTradeType()));
			app.setName(strategyConfig.getName());
			log.info("Starting " + strategyConfig.getName() + " strategy");
			app.start();
		}
	}

}
