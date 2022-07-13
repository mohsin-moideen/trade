package org.trade.utils.meta_api.listeners;

import java.util.concurrent.CompletableFuture;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ta4j.core.Trade.TradeType;
import org.trade.core.FxTradingRecord;
import org.trade.utils.JsonUtils;
import org.trade.utils.TelegramUtils;
import org.trade.utils.TrendAnalysis;
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
	private static final double triggerMultiplier = 2;
	private static final int PRICES_COUNT = 8;
	private String threadName;
	private TrendAnalysis trendAnalysis;
	private int priceTicker;
	private static MetatraderSymbolPrice price;
	private String symbol;

	public QuoteListener(FxTradingRecord tradingRecord, String threadName, String symbol) {
		super();
		this.tradingRecord = tradingRecord;
		this.tradeType = tradingRecord.getStartingType();
		this.threadName = threadName;
		tradingRecord.setQuoteListener(this);
		trendAnalysis = new TrendAnalysis();
		priceTicker = 0;
		this.symbol = symbol;
	}

	public static MetatraderSymbolPrice getPrice() {
		return JsonUtils.copyObject(price, MetatraderSymbolPrice.class);
	}

	private static final Logger log = LogManager.getLogger(QuoteListener.class);

	@Override
	public CompletableFuture<Void> onSymbolPriceUpdated(String instanceIndex, MetatraderSymbolPrice symbolPrice) {
		MetatraderPosition openPosition = tradingRecord.getCurrentPosition().getMtPosition();
		if (symbolPrice.symbol.equals(symbol)) {
			Thread.currentThread().setName(threadName);
			price = symbolPrice;
			log.info(symbol + "price updated " + JsonUtils.getString(symbolPrice));
		}
		if (openPosition == null || !symbolPrice.symbol.equals(openPosition.symbol)) // symbol check for redundancy
			return CompletableFuture.completedFuture(null);
		log.debug(symbolPrice.symbol + " price updated " + JsonUtils.getString(symbolPrice));
		Double currentPrice;
		Double counterOrderPrice;
		TradeType actionType;
		if (tradeType == TradeType.BUY) {
			currentPrice = symbolPrice.bid;
			counterOrderPrice = symbolPrice.ask;
			actionType = TradeType.SELL;
		} else {
			currentPrice = symbolPrice.ask;
			counterOrderPrice = symbolPrice.bid;
			actionType = TradeType.BUY;
		}
		openPosition.currentPrice = currentPrice;
		trendAnalysis.updateTrend(currentPrice);
		log.debug("currentPrice = " + currentPrice);
		log.debug("openPosition.openPrice = " + openPosition.openPrice);
		log.debug("openPosition.volume = " + openPosition.volume);
		double currentProfit = TradeUtil.getProfit(openPosition.openPrice, openPosition.volume, currentPrice,
				tradeType);
		currentProfit = TradeUtil.roundOff(currentProfit, 2);

		log.info("current profit = " + currentProfit);
		if (openPosition != null && counterPosition == null) {
			if (shouldOpenCounterTrade(openPosition, currentProfit, actionType)) {
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
						resetTrend();
						TelegramUtils.sendMessage("Counter trade opened\nStrategy: " + Thread.currentThread().getName()
								+ "\nPosition type: " + actionType + "\nEntry price: " + counterPosition.openPrice);
					}
				} catch (Exception e) {
					log.error("Failed to place counter trade", e);
					// counterPosition = null;// unblocking counter trade creation due to failure
				}
			}
		} else if (openPosition != null && counterPosition != null) {
			priceTicker++;
			counterPosition.currentPrice = counterOrderPrice;
			if (shouldCloseCounterTrade(counterPosition, actionType, counterOrderPrice)) {
				closeCounterTrade();
			}
		}
		return CompletableFuture.completedFuture(null);
	}

	private void resetTrend() {
		priceTicker = 0;
		trendAnalysis = new TrendAnalysis();
	}

	private boolean shouldOpenCounterTrade(MetatraderPosition openPosition, double currentProfit,
			TradeType counterOrderType) {
		double triggerLoss = getCounterTradeTriggerLoss(openPosition.volume);
		log.info("counter order trigger loss = " + triggerLoss);
		return currentProfit <= triggerLoss && isTrendAligned(counterOrderType)
				&& trendAnalysis.getTrendStrength() >= .1;
	}

	private boolean shouldCloseCounterTrade(MetatraderPosition counterPosition, TradeType counterOrderType,
			double counterOrderPrice) {
		double counterOrderProfit = TradeUtil.getProfit(counterPosition.openPrice, counterPosition.volume,
				counterOrderPrice, counterOrderType);
		log.info("counter Order  profit = " + counterOrderProfit);
		if (priceTicker < PRICES_COUNT) {
			return false;
		}
//		if (counterOrderProfit <= -(counterPosition.volume * 20)) {
//			return true;
//		}
		return !isTrendAligned(counterOrderType) && counterOrderProfit >= 0 && trendAnalysis.getTrendStrength() > 0.08;

	}

	private boolean isTrendAligned(TradeType orderType) {
		if (orderType == TradeType.BUY)
			return trendAnalysis.isBullish();
		else
			return !trendAnalysis.isBullish();
	}

	// Talk to roof about this
	private double getCounterTradeTriggerLoss(double volume) {
		return -Math.min((10 * volume * triggerMultiplier), (100 * volume));
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
			log.info("closing counter trade");
			MetaApiUtil.getMetaApiConnection().closePosition(counterPosition.id, null);
			resetTrend();
			TelegramUtils.sendMessage("Counter trade closed\nStrategy: " + Thread.currentThread().getName()
					+ "\nPosition type: " + tradeType.complementType() + "\nEntry price: " + counterPosition.openPrice
					+ "\nExit price: " + counterPosition.currentPrice + "\nCounter trade profit: $"
					+ TradeUtil.getProfit(counterPosition.openPrice, counterPosition.volume,
							counterPosition.currentPrice, tradeType.complementType()));
			counterPosition = null; // clearing counter position to open if price falls
		}
		return false;
	}

}
