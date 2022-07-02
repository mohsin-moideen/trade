package org.trade;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import org.trade.utils.GoogleAuthUtils;
import org.trade.utils.TelegramUtils;

import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;

public class Bot {

	private static final Logger log = LogManager.getLogger(Bot.class);

	private static Map<String, App> apps = new HashMap<>();
	private static ExecutorService executorService = Executors.newFixedThreadPool(8);

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
		BarSeries gbpusdSeries1 = SeriesUtil.initMovingBarSeries(SYMBOL_GBPUSD, timeframe, 500);
		BarSeries gbpusdSeries2 = SeriesUtil.initMovingBarSeries(SYMBOL_GBPUSD, timeframe, 500);
		Strategy strategy3 = Strategies.getVwap9EmaSellStrategy(gbpusdSeries1, DecimalNum.valueOf(0.2),
				DecimalNum.valueOf(0.09));
		Strategy strategy4 = Strategies.getVwap9EmaBuyStrategy(gbpusdSeries2, DecimalNum.valueOf(0.07),
				DecimalNum.valueOf(0.09));

		List<StrategyConfig> strategyConfigs = new LinkedList<>();

//		strategyConfigs.add(new StrategyConfig("EURUSD-Vwap9EmaSell", SYMBOL_EURUSD, timeframe, volume, eurusdSeries1,
//				strategy1, TradeType.SELL));
//		strategyConfigs.add(new StrategyConfig("EURUSD-Vwap9EmaBuy", SYMBOL_EURUSD, timeframe, volume, eurusdSeries2,
//				strategy2, TradeType.BUY));
//
		strategyConfigs.add(new StrategyConfig("GBPUSD-Vwap9EmaSell", SYMBOL_GBPUSD, timeframe, volume, gbpusdSeries1,
				strategy3, TradeType.SELL));
		strategyConfigs.add(new StrategyConfig("GBPUSD-Vwap9EmaSell", SYMBOL_GBPUSD, timeframe, volume, gbpusdSeries2,
				strategy4, TradeType.SELL));

		for (StrategyConfig strategyConfig : strategyConfigs) {
			App app = new App(strategyConfig.getSymbol(), strategyConfig.getTimeframe(), strategyConfig.getVolume(),
					strategyConfig.getSeries(), strategyConfig.getStrategy(), strategyConfig.getTradeType());
			Thread thread = new Thread(app);
			thread.setName(strategyConfig.getName());
			log.info("Starting " + strategyConfig.getName() + " strategy");
			apps.put(strategyConfig.getName(), app);
			executorService.execute(thread);
		}
		setTelegramListener();

	}

	private static void setTelegramListener() {
		final String STRATEGY = "STRATEGY";
		final String BOT = "BOT";
		final String PAUSE = "PAUSE";
		final String RESUME = "RESUME";

		TelegramUtils.setUpdatesListener(new UpdatesListener() {
			@Override
			public int process(List<Update> updates) {

				System.out.println(updates);
				for (Update update : updates) {
					String[] tokens = update.message().text().trim().split(" ");
					String auth = tokens[tokens.length - 1];
					String fromUser = update.message().from().username();
					String chatId = update.message().chat().id().toString();

					if (!isAuthValid(auth, fromUser)) {
						TelegramUtils.sendMessage("Invalid auth");
						TelegramUtils.sendMessage("Request -> " + update.toString());
						break;
					}
					if (tokens[0].equals(BOT)) {
						switch (tokens[1]) {
						case PAUSE: {
							for (App app : apps.values()) {
								app.pause();
							}
							TelegramUtils.sendMessage("Bot paused by " + fromUser);
							break;

						}
						case RESUME: {
							for (App app : apps.values()) {
								app.resume();
							}
							TelegramUtils.sendMessage("Bot resumed by " + fromUser);
							break;

						}
						default: {
							TelegramUtils.sendMessage("Invalid action " + tokens[1], chatId);
						}
						}
					} else if (tokens[0].equals(STRATEGY)) {
						App app = apps.get(tokens[1]);
						if (app == null) {
							TelegramUtils.sendMessage("Invalid strategy " + tokens[1], chatId);
						}
						switch (tokens[2]) {
						case PAUSE: {
							app.pause();
							TelegramUtils.sendMessage("Straegy " + tokens[1] + " paused by " + fromUser);
							break;
						}
						case RESUME: {
							app.resume();
							TelegramUtils.sendMessage("Straegy " + tokens[1] + " resumed by " + fromUser);
							break;
						}
						default: {
							TelegramUtils.sendMessage("Invalid action " + tokens[2], chatId);
						}
						}
					} else {
						TelegramUtils.sendMessage("Invalid action object " + tokens[0], chatId);

					}
				}
				return UpdatesListener.CONFIRMED_UPDATES_ALL;
			}

			private boolean isAuthValid(String auth, String fromUser) {
				try {
					int otp = Integer.parseInt(auth);
					return getValidUserIds().contains(fromUser) && GoogleAuthUtils.authorize(fromUser, otp);
				} catch (Exception e) {
					Thread.currentThread().setName("TelegramUtils");
					log.info("Missing otp");
				}
				return false;
			}

			private Set<String> getValidUserIds() {
				// TODO make this list configurable
				Set<String> validUsers = new HashSet<>();
				validUsers.add("mo9001");
				return validUsers;
			}
		});

	}

}
