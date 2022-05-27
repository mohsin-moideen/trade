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
	private static Queue<Double> prices;

	public QuoteListener(FxTradingRecord tradingRecord) {
		super();
		this.tradingRecord = tradingRecord;
		this.tradeType = tradingRecord.getStartingType();
		initTriggerPoints();
		prices = new CircularFifoQueue<Double>(10);
	}

	private void initTriggerPoints() {
		triggerMultiplier = 1;
	}

	private static final Logger log = LogManager.getLogger(QuoteListener.class);

	@Override
	public CompletableFuture<Void> onSymbolPriceUpdated(String instanceIndex, MetatraderSymbolPrice price) {
		MetatraderPosition openPosition = tradingRecord.getCurrentPosition().getMtPosition();
		if (openPosition == null || !price.symbol.equals(openPosition.symbol)) // symbol check for redundancy
			return CompletableFuture.completedFuture(null);
		log.info(price.symbol + " price updated " + JsonUtils.getString(price));
		Double currentPrice;
		Double counterOrderPrice;
		final int LOT_SIZE = 100000; // TODO: get from meta api for other pairs
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
		double currentProfit = getProfit(openPosition.openPrice, openPosition.volume, currentPrice, LOT_SIZE,
				tradeType);
		currentProfit = Math.round(currentProfit * 100.0) / 100.0;

		log.info("current profit = " + currentProfit);
		if (openPosition != null && counterPosition == null) {
			double triggerLoss = getCounterTradeTriggerLoss(openPosition.volume);
			log.info("counter order trigger Price = " + triggerLoss);
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
						triggerMultiplier += 0.5;
					}
				} catch (Exception e) {
					log.error("Failed to place counter trade", e);
					counterPosition = null;// unblocking counter trade creation due to failure
				}
			}
		} else if (openPosition != null && counterPosition != null) {
			double counterOrderProfit = getProfit(counterPosition.openPrice, counterPosition.volume, currentPrice,
					LOT_SIZE, actionType);
			log.info("counter Order  profit = " + counterOrderProfit);
			if (shouldCloseCounterTrade(counterOrderProfit, actionType)) {
				log.info("closing counter trade");
				MetaApiUtil.getMetaApiConnection().closePosition(counterPosition.id, null);
				counterPosition = null; // clearing counter position to open if price falls
			}
		}
		return CompletableFuture.completedFuture(null);
	}

	private boolean shouldCloseCounterTrade(double counterOrderProfit, TradeType actionType) {
		if (actionType == TradeType.BUY)
			return counterOrderProfit <= 0.2 && prices.size() == 10 && !isBullish();
		else
			return counterOrderProfit <= 0.2 && prices.size() == 10 && isBullish();

	}

	public static void main(String[] args) {
		prices = new CircularFifoQueue<Double>(10);
		prices.add(1.0);
		prices.add(0.5);
		prices.add(1.3);
		prices.add(0.7);
		prices.add(1.2);
		prices.add(1.1);
		prices.add(1.5);
		prices.add(1.4);
		System.out.println(isBullish());
	}

	private static boolean isBullish() {
		double trend = 0.0;
		List<Double> priceList = new LinkedList<>(prices);
		for (int i = 1; i < priceList.size(); i++) {
			trend += (priceList.get(i) - priceList.get(i - 1));
		}
		log.info("Trend = " + trend);
		return trend > 0;
	}

	// Talk to roof about this
	private double getCounterTradeTriggerLoss(double volume) {
		return Math.min((10 * volume * triggerMultiplier), (100 * volume));
	}

	private double getProfit(double openPrice, double volume, Double currentPrice, final int lotSize,
			TradeType tradeType) {
		if (tradeType == TradeType.BUY)
			return (currentPrice - openPrice) * (volume * lotSize);
		else
			return (openPrice - currentPrice) * (volume * lotSize);
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
