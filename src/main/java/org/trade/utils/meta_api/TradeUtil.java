package org.trade.utils.meta_api;

import java.util.concurrent.ExecutionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ta4j.core.Trade.TradeType;
import org.trade.utils.JsonUtils;
import org.trade.utils.meta_api.beans.TradeRequest;

import cloud.metaapi.sdk.clients.meta_api.TradeException;
import cloud.metaapi.sdk.clients.meta_api.models.MetatraderTradeResponse;
import cloud.metaapi.sdk.clients.meta_api.models.StopOptions;
import cloud.metaapi.sdk.clients.meta_api.models.StopOptions.StopUnits;
import cloud.metaapi.sdk.meta_api.MetaApiConnection;

public class TradeUtil {

	private static final Logger log = LogManager.getLogger(TradeUtil.class);

	public static MetatraderTradeResponse createOrder(TradeRequest tradeRequest) throws TradeException {
		if (!validateTradeRequest(tradeRequest)) {
			log.error("Invalid trade request " + tradeRequest);
			return null;
		}
		MetaApiConnection connection = MetaApiUtil.getMetaApiConnection();
		MetatraderTradeResponse order = null;
		log.info("Placing order " + tradeRequest);
		try {
			StopOptions stopLoss = new StopOptions();
			stopLoss.value = tradeRequest.getStopLoss() == null ? 0 : tradeRequest.getStopLoss();
			stopLoss.units = tradeRequest.getStopLossUnits() == null ? StopUnits.ABSOLUTE_PRICE
					: tradeRequest.getStopLossUnits();

			StopOptions takeProfit = new StopOptions();
			takeProfit.value = tradeRequest.getTakeProfit() == null ? 0 : tradeRequest.getTakeProfit();
			takeProfit.units = tradeRequest.getTakeProfitUnits() == null ? StopUnits.ABSOLUTE_PRICE
					: tradeRequest.getTakeProfitUnits();
			if (tradeRequest.getOpenPrice() == null) {
				if (tradeRequest.getActionType() == TradeType.BUY) {
					order = connection.createMarketBuyOrder(tradeRequest.getSymbol(), tradeRequest.getVolume(),
							stopLoss, takeProfit, null).get();
				} else {
					order = connection.createMarketSellOrder(tradeRequest.getSymbol(), tradeRequest.getVolume(),
							stopLoss, takeProfit, null).get();
				}
			} else {
				if (tradeRequest.getActionType() == TradeType.BUY) {
					order = connection.createLimitBuyOrder(tradeRequest.getSymbol(), tradeRequest.getVolume(),
							tradeRequest.getOpenPrice(), stopLoss, takeProfit, null).get();
				} else {
					order = connection.createLimitSellOrder(tradeRequest.getSymbol(), tradeRequest.getVolume(),
							tradeRequest.getOpenPrice(), stopLoss, takeProfit, null).get();
				}
			}
			log.info("Order placed " + JsonUtils.getString(order));
			return order;
		} catch (ExecutionException e) {
			if (e.getCause() instanceof TradeException) {
				throw (TradeException) e.getCause();
			}
		} catch (Exception e) {
			log.error("Failed to create order", e);
		}
		return order;
	}

	private static boolean validateTradeRequest(TradeRequest tradeRequest) {
		if (tradeRequest.getActionType() == null || tradeRequest.getVolume() == null
				|| tradeRequest.getSymbol() == null)
			return false;
		return true;
	}

	public static double roundOff(double number, int points) {
		double equilizer = Math.pow(10, points);
		return Math.round(number * equilizer) / equilizer;
	}

	public static double getProfit(Double openPrice, Double volume, Double currentPrice, TradeType tradeType,
			int LOT_SIZE) {
		if (tradeType == TradeType.BUY)
			return roundOff((currentPrice - openPrice) * (volume * LOT_SIZE), 2);
		else
			return roundOff((openPrice - currentPrice) * (volume * LOT_SIZE), 2);
	}

	public static void main(String[] args) {
		TradeRequest request = new TradeRequest();
		request.setOpenPrice(1.0585);
		request.setVolume(0.01);
		request.setSymbol("EURUSD");
		request.setActionType(TradeType.BUY);
		try {
			createOrder(request);
		} catch (TradeException e) {
			// invalid price code from meta api
			if (e.numericCode == 10015) {
				request.setOpenPrice(null);
				try {
					createOrder(request);
				} catch (TradeException e1) {
					log.error("Failed to place market order");
				}
			}
		}
	}
}
