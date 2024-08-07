package org.trade.utils.meta_api.listeners;

import java.util.concurrent.CompletableFuture;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ta4j.core.BarSeries;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import org.trade.core.FxTradingRecord;
import org.trade.utils.JsonUtils;
import org.trade.utils.TelegramUtils;
import org.trade.utils.meta_api.MetaApiUtil;
import org.trade.utils.meta_api.TradeUtil;

import cloud.metaapi.sdk.clients.meta_api.SynchronizationListener;
import cloud.metaapi.sdk.clients.meta_api.models.MetatraderPosition;
import cloud.metaapi.sdk.meta_api.MetaApiConnection;

public class OrderSynchronizationListener extends SynchronizationListener {

	/*
	 * change this to map of orderId, Position object. update position object when
	 * order is updated
	 */
	private FxTradingRecord tradingRecord;
	private BarSeries series;
	private Num volume;
	private String threadName;

	public OrderSynchronizationListener(BarSeries series, Num volume, FxTradingRecord tradingRecord,
			String threadName) {
		super();
		this.tradingRecord = tradingRecord;
		this.series = series;
		this.volume = volume;
		this.threadName = threadName;

	}

	private static final Logger log = LogManager.getLogger(OrderSynchronizationListener.class);

	@Override
	public CompletableFuture<Void> onOrderCompleted(String instanceIndex, String orderId) {
		Thread.currentThread().setName(threadName);
		log.info("Order completeted. order id " + orderId);
		try {
			if (!orderId.equals(tradingRecord.getCurrentPosition().getMtOrderId())) {
				return CompletableFuture.completedFuture(null);
			}
			MetaApiConnection connection = MetaApiUtil.getMetaApiConnection();
			MetatraderPosition position = connection.getPosition(orderId).get();
			log.info("Position found " + JsonUtils.getString(position));
			tradingRecord.getCurrentPosition().setMtPosition(position);
			log.info("Fx position updated with mt position");
		} catch (Exception e) {
			log.error("<<<<<<<<<<< FAILED TO UPDATE POSITION ID FOR ORDER " + orderId + " >>>>>>>>>>>>>>>>>", e);

		}
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public CompletableFuture<Void> onPositionRemoved(String instanceIndex, String positionId) {
		Thread.currentThread().setName(threadName);
		log.info("Position closed manually! Exiting current position " + positionId);
		MetatraderPosition openPosition = tradingRecord.getCurrentPosition().getMtPosition();
		log.info("Position open " + JsonUtils.getString(openPosition));

		if (openPosition == null || !openPosition.id.equals(positionId)) {
			return CompletableFuture.completedFuture(null);
		}
		try {
			tradingRecord.forcedExit(series.getEndIndex(), DecimalNum.valueOf(openPosition.currentPrice), volume);
			log.info("Current position exited!");
			log.info("Closing price " + openPosition.currentPrice);
			TelegramUtils.sendMessage("Trade closed\nStrategy: " + Thread.currentThread().getName()
					+ "\nPosition type: " + tradingRecord.getStartingType() + "\nEntry price: " + openPosition.openPrice
					+ "\nExit price: " + openPosition.currentPrice + "\nprofit: $"
					+ TradeUtil.getProfit(openPosition.openPrice, volume.doubleValue(), openPosition.currentPrice,
							tradingRecord.getStartingType()));
		} catch (Exception e) {
			log.error("<<<<<<<<<<< FAILED TO UPDATE TRADE FOR POSITION " + positionId + " >>>>>>>>>>>>>>>>>", e);

		}
		return CompletableFuture.completedFuture(null);

	}
}
