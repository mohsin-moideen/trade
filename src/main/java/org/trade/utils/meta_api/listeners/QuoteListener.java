package org.trade.utils.meta_api.listeners;

import java.util.concurrent.CompletableFuture;

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
	private double previousTrigger;
	private int triggerMultiplier;

	public QuoteListener(FxTradingRecord tradingRecord) {
		super();
		this.tradingRecord = tradingRecord;
		this.tradeType = tradingRecord.getStartingType();
		previousTrigger = 0;
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
		log.info("currentPrice = " + currentPrice);
		log.info("openPosition.openPrice = " + openPosition.openPrice);
		log.info("openPosition.volume = " + openPosition.volume);
		double currentProfit = getProfit(openPosition.openPrice, openPosition.volume, currentPrice, LOT_SIZE,
				tradeType);
		currentProfit = Math.round(currentProfit * 100.0) / 100.0;

		log.info("current profit = " + currentProfit);
		if (openPosition != null && counterPosition == null) {
			double triggerPrice = getCounterTradeTriggerPrice(openPosition.volume);
			if (currentProfit <= triggerPrice) {
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
						previousTrigger = triggerPrice;
						triggerMultiplier++;
					}
				} catch (Exception e) {
					log.error("Failed to place counter trade", e);
					counterPosition = null;// unblocking counter trade creation due to failure
				}
			}
		} else if (openPosition != null && counterPosition != null) {
			double counterOrderProfit = getProfit(counterPosition.openPrice, counterPosition.volume, counterOrderPrice,
					LOT_SIZE, actionType);
			log.info("counter Order  profit = " + counterOrderProfit);
			if (counterOrderProfit <= 0.2) {
				log.info("closing counter trade");
				MetaApiUtil.getMetaApiConnection().closePosition(counterPosition.id, null);
				counterPosition = null; // clearing counter position to open if price falls
			}
		}
		return CompletableFuture.completedFuture(null);
	}

	// Talk to roof about this
	private double getCounterTradeTriggerPrice(double volume) {
		return Math.min(previousTrigger - (10 * volume * triggerMultiplier), (100 * volume));
	}

	private double getProfit(double openPrice, double volume, Double currentPrice, final int LOT_SIZE,
			TradeType tradeType) {
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
