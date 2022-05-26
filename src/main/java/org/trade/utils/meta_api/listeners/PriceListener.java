package org.trade.utils.meta_api.listeners;

import java.util.concurrent.CompletableFuture;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Trade.TradeType;
import org.trade.utils.JsonUtils;

import cloud.metaapi.sdk.clients.meta_api.SynchronizationListener;
import cloud.metaapi.sdk.clients.meta_api.models.MetatraderSymbolPrice;

public class PriceListener extends SynchronizationListener {

	private TradeType tradeType;
	private BarSeries series;
	private String symbol;

	public BarSeries getSeries() {
		return series;
	}

	public String getSymbol() {
		return symbol;
	}

	public PriceListener(BarSeries series, String symbol, TradeType tradeType) {
		super();
		this.series = series;
		this.symbol = symbol;
		this.tradeType = tradeType;
	}

	private static final Logger log = LogManager.getLogger(PriceListener.class);

	@Override
	public CompletableFuture<Void> onSymbolPriceUpdated(String instanceIndex, MetatraderSymbolPrice price) {
		if (!price.symbol.equals(symbol))
			return CompletableFuture.completedFuture(null);
		log.info(price.symbol + " price updated " + JsonUtils.getString(price));
		Double currentPrice;
		if (tradeType == TradeType.BUY) {
			currentPrice = price.bid;
		} else {
			currentPrice = price.ask;
		}
		series.addPrice(currentPrice);
		return CompletableFuture.completedFuture(null);
	}

}
