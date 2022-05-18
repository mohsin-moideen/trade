package org.trade.utils.meta_api.listeners;

import java.util.concurrent.CompletableFuture;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.trade.core.FxTradingRecord;
import org.trade.utils.JsonUtils;
import org.trade.utils.meta_api.MetaApiUtil;

import cloud.metaapi.sdk.clients.meta_api.SynchronizationListener;
import cloud.metaapi.sdk.clients.meta_api.models.MetatraderPosition;
import cloud.metaapi.sdk.meta_api.MetaApiConnection;

public class OrderSynchronizationListener extends SynchronizationListener {

	/*
	 * change this to map of orderId, Position object. update position object when
	 * order is updated
	 */
	private FxTradingRecord tradingRecord;

	public OrderSynchronizationListener(FxTradingRecord tradingRecord) {
		super();
		this.tradingRecord = tradingRecord;
	}

	private static final Logger log = LogManager.getLogger(OrderSynchronizationListener.class);

	@Override
	public CompletableFuture<Void> onOrderCompleted(String instanceIndex, String orderId) {
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

}
