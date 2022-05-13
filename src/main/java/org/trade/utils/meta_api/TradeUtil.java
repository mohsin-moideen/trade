package org.trade.utils.meta_api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.trade.utils.meta_api.beans.TradeRequest;

import cloud.metaapi.sdk.clients.meta_api.models.MetatraderTradeResponse;
import cloud.metaapi.sdk.clients.meta_api.models.StopOptions;
import cloud.metaapi.sdk.clients.meta_api.models.StopOptions.StopUnits;
import cloud.metaapi.sdk.meta_api.MetaApiConnection;

public class TradeUtil {

	private static final Logger log = LogManager.getLogger(TradeUtil.class);

	public static MetatraderTradeResponse createLimitBuyOrder(TradeRequest tradeRequest) {
		MetaApiConnection connection = MetaApiUtil.getMetaApiConnection();
		MetatraderTradeResponse order = null;
		try {
			StopOptions stoploss = new StopOptions();
			stoploss.value = tradeRequest.getStopLoss() == null ? 0 : tradeRequest.getStopLoss();
			stoploss.units = tradeRequest.getStopLossUnits() == null ? StopUnits.ABSOLUTE_PRICE
					: tradeRequest.getStopLossUnits();

			StopOptions takeProfit = new StopOptions();
			takeProfit.value = tradeRequest.getTakeProfit() == null ? 0 : tradeRequest.getTakeProfit();
			takeProfit.units = tradeRequest.getTakeProfitUnits() == null ? StopUnits.ABSOLUTE_PRICE
					: tradeRequest.getTakeProfitUnits();

			order = connection.createLimitBuyOrder(tradeRequest.getSymbol(), tradeRequest.getVolume(),
					tradeRequest.getOpenPrice(), stoploss, null, null).get();
			log.info("Order placed " + order);
			return order;
		} catch (Exception e) {
			log.error("Failed to create limit buy order", e);
		}
		return order;
	}

	public static void main(String[] args) {
		TradeRequest request = new TradeRequest();
		request.setOpenPrice(1.0000);
		request.setVolume(0.01);
		request.setSymbol("EURUSD");
		createLimitBuyOrder(request);
	}

	public static MetatraderTradeResponse createLimitSellOrder(TradeRequest tradeRequest) {
		MetaApiConnection connection = MetaApiUtil.getMetaApiConnection();
		MetatraderTradeResponse order = null;
		try {
			StopOptions stoploss = new StopOptions();
			stoploss.value = tradeRequest.getStopLoss();
			stoploss.units = tradeRequest.getStopLossUnits();

			StopOptions takeProfit = new StopOptions();
			takeProfit.value = tradeRequest.getTakeProfit();
			takeProfit.units = tradeRequest.getTakeProfitUnits();

			order = connection.createLimitSellOrder(tradeRequest.getSymbol(), tradeRequest.getVolume(),
					tradeRequest.getOpenPrice(), stoploss, stoploss, null).get();
			log.info("Order placed " + order);
			return order;
		} catch (Exception e) {
			log.error("Failed to create limit buy order", e);
		}
		return order;
	}
}
