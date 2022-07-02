package org.trade;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Strategy;
import org.ta4j.core.Trade;
import org.ta4j.core.Trade.TradeType;
import org.ta4j.core.num.Num;
import org.trade.config.Constants;
import org.trade.core.FxStrategy;
import org.trade.core.FxTradingRecord;
import org.trade.core.beans.Candle;
import org.trade.enums.Timeframe;
import org.trade.utils.TelegramUtils;
import org.trade.utils.meta_api.MarketDataUtil;
import org.trade.utils.meta_api.MetaApiUtil;
import org.trade.utils.meta_api.TradeUtil;
import org.trade.utils.meta_api.listeners.OrderSynchronizationListener;
import org.trade.utils.meta_api.listeners.QuoteListener;

/**
 * This class is an example of a dummy trading bot using ta4j.
 * <p/>
 */
public class App implements Runnable {

	private static final Logger log = LogManager.getLogger(App.class);
	private volatile boolean running = true;
	private volatile boolean paused = false;
	private final Object pauseLock = new Object();

	private String symbol;
	private Timeframe timeframe;
	private Num volume;
	private BarSeries series;
	private Strategy strategy;
	// private boolean forcedExit;
	private TradeType tradeType;

	public App(String symbol, Timeframe timeframe, Num volume, BarSeries series, Strategy strategy,
			TradeType tradeType) {
		super();
		this.symbol = symbol;
		this.timeframe = timeframe;
		this.volume = volume;
		this.series = series;
		this.strategy = strategy;
		this.tradeType = tradeType;
		// forcedExit = false;
	}

	private void updateSeries() {
		Candle candle = MarketDataUtil.getCurrentCandle(symbol, timeframe);
		// null check for candle - happens when meta api is down!!
		if (candle == null || candle.getTime() == null) {
			log.error("Failed to fetch candle data from Meta api./n Exiting all trades!");
			TelegramUtils.sendMessage("⚠️⚠️⚠️⚠️⚠️  Failed to fetch candle data from Meta api. ⚠️⚠️⚠️⚠️⚠️");
			TelegramUtils.sendMessage("⚠️⚠️⚠️⚠️⚠️ Please ensure Meta api server is running ⚠️⚠️⚠️⚠️⚠️");
			TelegramUtils.sendMessage("Do not ignore this message!");
			return;
		}
		if (candle.getZonedDate().isAfter(series.getLastBar().getEndTime())) {
			try {
				series.addBar(candle.getZonedDate(), candle.getOpen(), candle.getHigh(), candle.getLow(),
						candle.getClose(), candle.getTickVolume());
				log.info("series length " + series.getBarCount());

			} catch (Exception e) {
				log.error("Failed to add bar to series", e);
				log.info("Will retry in next cycle");
			}

		}
	}

	@Override
	public void run() {
		MetaApiUtil.initMetaApi();

		FxTradingRecord tradingRecord = new FxTradingRecord(symbol, tradeType);

		MetaApiUtil.getMetaApiConnection().addSynchronizationListener(
				new OrderSynchronizationListener(series, volume, tradingRecord, Thread.currentThread().getName()));
		QuoteListener quoteListener = new QuoteListener(tradingRecord, Thread.currentThread().getName());
		MetaApiUtil.getMetaApiConnection().addSynchronizationListener(quoteListener);

		log.info("Initialization complete");

		while (running) {
			synchronized (pauseLock) {
				if (!running) { // may have changed while waiting to
					// synchronize on pauseLock
					break;
				}
				if (paused) {
					try {
						pauseLock.wait(); // will cause this Thread to block until
						// another thread calls pauseLock.notifyAll()
						// Note that calling wait() will
						// relinquish the synchronized lock that this
						// thread holds on pauseLock so another thread
						// can acquire the lock to call notifyAll()
						// (link with explanation below this code)
					} catch (InterruptedException ex) {
						break;
					}
					if (!running) { // running might have changed since we paused
						break;
					}
				}
			}
			updateSeries();
			Num lastClosePrice = series.getLastBar().getClosePrice();
			log.info("Bar added, close price = " + lastClosePrice);
			int endIndex = series.getEndIndex();
			if (strategy.shouldEnter(endIndex)) {
				// Our strategy should enter
				boolean entered = tradingRecord.enter(endIndex, lastClosePrice, volume, getStopLoss(strategy),
						getTakeProfit(strategy));
				if (entered) {
					Trade entry = tradingRecord.getLastEntry();
					log.info("Entered on " + entry.getIndex() + " (price=" + entry.getNetPrice().doubleValue()
							+ ", amount=" + entry.getAmount().doubleValue() + ")");
					TelegramUtils.sendMessage("Position entered\nStrategy: " + Thread.currentThread().getName()
							+ "\nPosition type: " + tradingRecord.getStartingType() + "\nEntry price: "
							+ entry.getNetPrice().doubleValue());
				}
			} else if (strategy.shouldExit(endIndex)) {
				// Our strategy should exit
				boolean exited = tradingRecord.exit(endIndex, lastClosePrice, volume);
				if (exited) {
					Trade exit = tradingRecord.getLastExit();
					log.info("Exited on " + exit.getIndex() + " (price=" + exit.getNetPrice().doubleValue()
							+ ", amount=" + exit.getAmount().doubleValue() + ")");
					TelegramUtils.sendMessage("Position exited\nStrategy: " + Thread.currentThread().getName()
							+ "\nPosition type: " + tradingRecord.getStartingType() + "\nExit price: "
							+ exit.getNetPrice().doubleValue() + "\nProfit: $"
							+ TradeUtil.getProfit(tradingRecord.getLastEntry().getNetPrice().doubleValue(),
									volume.doubleValue(), exit.getNetPrice().doubleValue(),
									tradingRecord.getStartingType(), Constants.LOT_SIZE));
				}
			}
			try {
				log.info("Thread paused for " + Timeframe.getMintues(timeframe) + " minutes");
				Thread.sleep(Timeframe.getMintues(timeframe) * 60000);
			} catch (InterruptedException e) {
				log.error(e.getMessage(), e);
			}
		}
	}

	public void stop() {
		running = false;
		// you might also want to interrupt() the Thread that is
		// running this Runnable, too, or perhaps call:
		resume();
		// to unblock
	}

	public void pause() {
		// you may want to throw an IllegalStateException if !running
		paused = true;
	}

	public void resume() {
		synchronized (pauseLock) {
			paused = false;
			pauseLock.notifyAll(); // Unblocks thread
		}
	}

	private Double getTakeProfit(Strategy strategy) {
		if (strategy instanceof FxStrategy) {
			return ((FxStrategy) strategy).getStopLoss();
		}
		return null;
	}

	private Double getStopLoss(Strategy strategy) {
		if (strategy instanceof FxStrategy) {
			return ((FxStrategy) strategy).getTakeProfit();
		}
		return null;
	}
}
