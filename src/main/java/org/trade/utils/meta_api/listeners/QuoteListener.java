package org.trade.utils.meta_api.listeners;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ta4j.core.Trade.TradeType;
import org.trade.config.Constants;
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
	private static final int PRICES_COUNT = 12;
	private String threadName;

	public QuoteListener(FxTradingRecord tradingRecord, String threadName) {
		super();
		this.tradingRecord = tradingRecord;
		this.tradeType = tradingRecord.getStartingType();
		initTriggerPoints();
		prices = new CircularFifoQueue<Double>(PRICES_COUNT);
		this.threadName = threadName;
		tradingRecord.setQuoteListener(this);

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
		openPosition.currentPrice = currentPrice;
		prices.add(currentPrice);
		log.debug("currentPrice = " + currentPrice);
		log.debug("openPosition.openPrice = " + openPosition.openPrice);
		log.debug("openPosition.volume = " + openPosition.volume);
		double currentProfit = TradeUtil.getProfit(openPosition.openPrice, openPosition.volume, currentPrice, tradeType,
				Constants.LOT_SIZE);
		currentProfit = TradeUtil.roundOff(currentProfit, 2);

		log.info("current profit = " + currentProfit);
		if (openPosition != null && counterPosition == null) {
			double triggerLoss = getCounterTradeTriggerLoss(openPosition.volume);
			log.info("counter order trigger loss = " + triggerLoss);
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
			counterPosition.currentPrice = counterOrderPrice;
			if (shouldCloseCounterTrade(counterPosition, actionType, counterOrderPrice)) {
				closeCounterTrade();
			}
		}
		return CompletableFuture.completedFuture(null);
	}

	private boolean shouldCloseCounterTrade(MetatraderPosition counterPosition, TradeType actionType,
			double counterOrderPrice) {
		double counterOrderProfit = TradeUtil.getProfit(counterPosition.openPrice, counterPosition.volume,
				counterOrderPrice, actionType, Constants.LOT_SIZE);
		log.info("counter Order  profit = " + counterOrderProfit);
		if (counterOrderProfit < -(counterPosition.volume * 10)) {
			return true;
		}
		if (actionType == TradeType.BUY)
			return isIncounterOrderProfitRange(counterPosition, counterOrderProfit) && !isBullish();
		else
			return isIncounterOrderProfitRange(counterPosition, counterOrderProfit) && isBullish();

	}

	private boolean isIncounterOrderProfitRange(MetatraderPosition counterPosition, double counterOrderProfit) {
		return counterOrderProfit >= 0 && counterOrderProfit <= (counterPosition.volume * 2)
				&& prices.size() >= PRICES_COUNT;
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
		for (int i = 1; i < priceList.size(); i++) {
			trend += (priceList.get(i) - priceList.get(i - 1));
		}
		log.info("Trend = " + TradeUtil.roundOff(trend, 5));
		return trend > 0;
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
			prices.clear();
			TelegramUtils.sendMessage("Counter trade closed\nStrategy: " + Thread.currentThread().getName()
					+ "\nPosition type: " + tradeType.complementType() + "\nEntry price: " + counterPosition.openPrice
					+ "\nExit price: " + counterPosition.currentPrice + "\nCounter trade profit: $"
					+ TradeUtil.getProfit(counterPosition.openPrice, counterPosition.volume,
							counterPosition.currentPrice, tradeType.complementType(), Constants.LOT_SIZE));
			counterPosition = null; // clearing counter position to open if price falls
		}
		return false;
	}

}
