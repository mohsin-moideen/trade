package org.trade.utils.meta_api.listeners;

import java.util.concurrent.CompletableFuture;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ta4j.core.Trade.TradeType;
import org.trade.utils.JsonUtils;
import org.trade.utils.meta_api.MetaApiUtil;
import org.trade.utils.meta_api.TradeUtil;
import org.trade.utils.meta_api.beans.TradeRequest;

import cloud.metaapi.sdk.clients.meta_api.SynchronizationListener;
import cloud.metaapi.sdk.clients.meta_api.models.MetatraderPosition;
import cloud.metaapi.sdk.clients.meta_api.models.MetatraderSymbolPrice;
import cloud.metaapi.sdk.clients.meta_api.models.MetatraderTradeResponse;

public class QuoteListener extends SynchronizationListener {

	private MetatraderPosition openPosition;
	private TradeType tradeType;
	private MetatraderPosition counterPosition;

	public QuoteListener(MetatraderPosition openPosition, TradeType tradeType) {
		super();
		this.openPosition = openPosition;
		this.tradeType = tradeType;
	}

	private static final Logger log = LogManager.getLogger(QuoteListener.class);

	@Override
	public CompletableFuture<Void> onSymbolPriceUpdated(String instanceIndex, MetatraderSymbolPrice price) {
		log.info(price.symbol + " price updated " + JsonUtils.getString(price));
		Double currentPrice;
		TradeType actionType;
		if (tradeType == TradeType.BUY) {
			currentPrice = price.bid;
			actionType = TradeType.SELL;
		} else {
			currentPrice = price.ask;
			actionType = TradeType.BUY;
		}

		if (openPosition != null && counterPosition == null) {
			if ((currentPrice - openPosition.openPrice) * openPosition.volume <= -5) {
				TradeRequest tradeRequest = new TradeRequest();
				tradeRequest.setActionType(actionType);
				tradeRequest.setSymbol(openPosition.symbol);
				tradeRequest.setVolume(openPosition.volume);
				try {
					MetatraderTradeResponse counterOrder = TradeUtil.createOrder(tradeRequest);
					if (counterOrder != null && counterOrder.positionId != null) {
						MetaApiUtil.getMetaApiConnection().getPosition(counterOrder.positionId).get();
					}
				} catch (Exception e) {
					log.error("Failed to place counter trade", e);
				}
			}
		} else if (openPosition != null && counterPosition != null) {
			if ((currentPrice - openPosition.openPrice) * openPosition.volume >= -2) {
				MetaApiUtil.getMetaApiConnection().closePosition(counterPosition.id, null);
				counterPosition = null; // clearing counter position to open if price falls
			}
		}
		return CompletableFuture.completedFuture(null);
	}
}
