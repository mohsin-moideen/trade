package org.trade;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

	private static ExecutorService executorService = Executors.newFixedThreadPool(8);

	public static void main(String[] args) {
		Map<String, App> apps = new HashMap<>();
		TelegramUtils.isDisabled = true;
		final String SYMBOL_EURUSD = "EURUSD";
		final String SYMBOL_GBPUSD = "GBPUSD";
		final String SYMBOL_EURJPY = "EURJPY";
		final String SYMBOL_AUDNZD = "AUDNZD";

		final Timeframe timeframe = Timeframe.one_min;
		final Num volume = DecimalNum.valueOf(0.2);

		BarSeries eurjpySeries1 = SeriesUtil.initMovingBarSeries(SYMBOL_EURJPY, timeframe, 500);
//		BarSeries eurusdSeries2 = SeriesUtil.initMovingBarSeries(SYMBOL_EURUSD, timeframe, 500);
//		Strategy strategy1 = Strategies.getVwap9EmaSellStrategy(eurusdSeries1, DecimalNum.valueOf(0.2),
//				DecimalNum.valueOf(0.09));
//		Strategy strategy2 = Strategies.getVwap9EmaBuyStrategy(eurusdSeries2, DecimalNum.valueOf(0.2),
//				DecimalNum.valueOf(0.09));
//
//		BarSeries gbpusdSeries1 = SeriesUtil.initMovingBarSeries(SYMBOL_GBPUSD, timeframe, 500);
//		BarSeries gbpusdSeries2 = SeriesUtil.initMovingBarSeries(SYMBOL_GBPUSD, timeframe, 500);
		Strategy strategy3 = Strategies.getVwap9EmaSellStrategy(eurjpySeries1, DecimalNum.valueOf(0.02),
				DecimalNum.valueOf(0.02));
//		Strategy strategy4 = Strategies.getVwap9EmaBuyStrategy(gbpusdSeries2, DecimalNum.valueOf(0.07),
//				DecimalNum.valueOf(0.09));

		List<StrategyConfig> strategyConfigs = new LinkedList<>();

//		strategyConfigs.add(new StrategyConfig("EURUSD-Vwap9EmaSell", SYMBOL_EURUSD, timeframe, volume, eurusdSeries1,
//				strategy1, TradeType.SELL));
//		strategyConfigs.add(new StrategyConfig("EURUSD-Vwap9EmaBuy", SYMBOL_EURUSD, timeframe, volume, eurusdSeries2,
//				strategy2, TradeType.BUY));
//
		strategyConfigs.add(new StrategyConfig("SYMBOL_EURJPY-Vwap9EmaSell", SYMBOL_EURJPY, timeframe, volume,
				eurjpySeries1, strategy3, TradeType.BUY));
//		strategyConfigs.add(new StrategyConfig("GBPUSD-Vwap9EmaSell", SYMBOL_GBPUSD, timeframe, volume, gbpusdSeries2,
//				strategy4, TradeType.BUY));

		for (StrategyConfig strategyConfig : strategyConfigs) {
			App app = new App(strategyConfig.getSymbol(), strategyConfig.getTimeframe(), strategyConfig.getVolume(),
					strategyConfig.getSeries(), strategyConfig.getStrategy(), strategyConfig.getTradeType());
			Thread thread = new Thread(app);
			thread.setName(strategyConfig.getName());
			log.info("Starting " + strategyConfig.getName() + " strategy");
			apps.put(strategyConfig.getName(), app);
			executorService.execute(thread);
		}
		TelegramUtils.setTelegramListener(apps);

	}

}
