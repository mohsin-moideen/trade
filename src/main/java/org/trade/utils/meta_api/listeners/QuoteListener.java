package org.trade.utils.meta_api.listeners;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ta4j.core.Trade.TradeType;
import org.trade.core.FxTradingRecord;
import org.trade.utils.JsonUtils;
import org.trade.utils.TelegramUtils;
import org.trade.utils.meta_api.MetaApiUtil;
import org.trade.utils.meta_api.TradeUtil;
import org.trade.utils.meta_api.beans.TradeRequest;

import cloud.metaapi.sdk.clients.meta_api.SynchronizationListener;
import cloud.metaapi.sdk.clients.meta_api.models.MetatraderPosition;
import cloud.metaapi.sdk.clients.meta_api.models.MetatraderSymbolPrice;
import cloud.metaapi.sdk.clients.meta_api.models.MetatraderTradeResponse;

public class QuoteListener extends SynchronizationListener {

	private TradeType tradeType;
	private MetatraderPosition counterPosition;
	private FxTradingRecord tradingRecord;
	private double triggerMultiplier;
	private Queue<Double> prices;
	private static final int PRICES_COUNT = 25;
	final int LOT_SIZE;
	private String threadName;

	public QuoteListener(FxTradingRecord tradingRecord, String threadName) {
		super();
		this.tradingRecord = tradingRecord;
		this.tradeType = tradingRecord.getStartingType();
		initTriggerPoints();
		prices = new CircularFifoQueue<Double>(PRICES_COUNT);
		LOT_SIZE = 100000; // TODO: get from meta api for other pairs
		this.threadName = threadName;
	}

	private void initTriggerPoints() {
		triggerMultiplier = 4;
	}

	private static final Logger log = LogManager.getLogger(QuoteListener.class);

	@Override
	public CompletableFuture<Void> onSymbolPriceUpdated(String instanceIndex, MetatraderSymbolPrice price) {
		MetatraderPosition openPosition = tradingRecord.getCurrentPosition().getMtPosition();
		if (openPosition == null || !price.symbol.equals(openPosition.symbol)) // symbol check for redundancy
			return CompletableFuture.completedFuture(null);
		Thread.currentThread().setName(threadName);
		log.debug(price.symbol + " price updated " + JsonUtils.getString(price));
		Double currentPrice;
		Double counterOrderPrice;
		TradeType actionType;
		if (tradeType == TradeType.BUY) {
			currentPrice = price.bid;
			counterOrderPrice = price.ask;
			actionType = TradeType.SELL;
		} else {
			currentPrice = price.ask;
			counterOrderPrice = price.bid;
			actionType = TradeType.BUY;
		}
		prices.add(currentPrice);
		log.debug("currentPrice = " + currentPrice);
		log.debug("openPosition.openPrice = " + openPosition.openPrice);
		log.debug("openPosition.volume = " + openPosition.volume);
		double currentProfit = getProfit(openPosition.openPrice, openPosition.volume, currentPrice, tradeType);
		currentProfit = roundOff(currentProfit, 2);

		log.debug("current profit = " + currentProfit);
		if (openPosition != null && counterPosition == null) {
			double triggerLoss = getCounterTradeTriggerLoss(openPosition.volume);
			log.debug("counter order trigger loss = " + triggerLoss);
			if (currentProfit <= triggerLoss) {
				counterPosition = new MetatraderPosition();// blocking duplicate counter trade creation
				log.info("placing counter trade");
				TradeRequest tradeRequest = new TradeRequest();
				tradeRequest.setActionType(actionType);
				tradeRequest.setSymbol(openPosition.symbol);
				tradeRequest.setVolume(openPosition.volume);
				try {
					MetatraderTradeResponse counterOrder = TradeUtil.createOrder(tradeRequest);
					if (counterOrder != null && counterOrder.orderId != null) {
						log.info("Counter order placed " + JsonUtils.getString(counterOrder));
						counterPosition = MetaApiUtil.getMetaApiConnection().getPosition(counterOrder.orderId).get();
						log.info("Counter position retrieved " + JsonUtils.getString(counterPosition));
						triggerMultiplier *= 1.5;
						log.info("counter order trigger loss updated to "
								+ getCounterTradeTriggerLoss(openPosition.volume));
						prices.clear();
						TelegramUtils.sendMessage("Counter trade opened\nStrategy: " + Thread.currentThread().getName()
								+ "\nPosition type: " + actionType + "\nEntry price: " + counterPosition.openPrice);
					}
				} catch (Exception e) {
					log.error("Failed to place counter trade", e);
					counterPosition = null;// unblocking counter trade creation due to failure
				}
			}
		} else if (openPosition != null && counterPosition != null) {

			if (shouldCloseCounterTrade(counterPosition, actionType, counterOrderPrice)) {
				log.info("closing counter trade");
				MetaApiUtil.getMetaApiConnection().closePosition(counterPosition.id, null);
				counterPosition = null; // clearing counter position to open if price falls
				prices.clear();
				TelegramUtils.sendMessage("Counter trade closed\nStrategy: " + Thread.currentThread().getName()
						+ "\nPosition type: " + actionType + "\nEntry price: " + counterOrderPrice);
			}
		}
		return CompletableFuture.completedFuture(null);
	}

	private static double roundOff(double number, int points) {
		double equilizer = Math.pow(10, points);
		return Math.round(number * equilizer) / equilizer;
	}

	private boolean shouldCloseCounterTrade(MetatraderPosition counterPosition, TradeType actionType,
			double counterOrderPrice) {
		double counterOrderProfit = getProfit(counterPosition.openPrice, counterPosition.volume, counterOrderPrice,
				actionType);
		log.info("counter Order  profit = " + counterOrderProfit);
		if (counterOrderProfit < -(counterPosition.volume * 10) * 2) {
			return true;
		}
		if (actionType == TradeType.BUY)
			return isIncounterOrderProfitRange(counterOrderProfit) && !isBullish();
		else
			return isIncounterOrderProfitRange(counterOrderProfit) && isBullish();

	}

	private boolean isIncounterOrderProfitRange(double counterOrderProfit) {
		return counterOrderProfit >= 0 && counterOrderProfit <= 0.2 && prices.size() >= PRICES_COUNT;
	}

	public static void main(String[] args) {
//		prices = new CircularFifoQueue<Double>(10);
//		prices.add(1.0512);
//		prices.add(0.0522);
//		prices.add(1.0502);
//		prices.add(0.0522);
//		prices.add(1.0502);
//		prices.add(1.0498);
//		prices.add(1.0488);
//		prices.add(1.0498);
//		System.out.println(isBullish());
		Queue<Double> prices = new CircularFifoQueue<Double>(PRICES_COUNT);
		prices.add(1.0);
		System.out.println(prices.size());
	}

	private boolean isBullish() {
		double trend = 0.0;
		List<Double> priceList = new LinkedList<>(prices);
		System.out.print(roundOff(priceList.get(0), 5) + ", ");
		for (int i = 1; i < priceList.size(); i++) {
			trend += (priceList.get(i) - priceList.get(i - 1));
			System.out.print(roundOff(priceList.get(i), 5) + ", ");
		}
		System.out.println();
		log.info("Trend = " + roundOff(trend, 5));
		return trend > 0;
	}

	// Talk to roof about this
	private double getCounterTradeTriggerLoss(double volume) {
		return -Math.min((10 * volume * triggerMultiplier), (100 * volume));
	}

	private double getProfit(double openPrice, double volume, Double currentPrice, TradeType tradeType) {
		if (tradeType == TradeType.BUY)
			return (currentPrice - openPrice) * (volume * LOT_SIZE);
		else
			return (openPrice - currentPrice) * (volume * LOT_SIZE);
	}

	/**
	 * Added for redundancy. When original trade is closed by strategy, counter
	 * trade would have been already closed
	 * 
	 * @return
	 */
	public boolean closeCounterTrade() {
		if (counterPosition == null)
			return true;
		else {
			MetaApiUtil.getMetaApiConnection().closePosition(counterPosition.id, null);
			counterPosition = null;
		}
		return false;
	}

}
