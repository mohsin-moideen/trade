package org.trade.utils.meta_api.beans;

import org.ta4j.core.Trade.TradeType;

import cloud.metaapi.sdk.clients.meta_api.models.StopOptions.StopUnits;

public class TradeRequest {

	private TradeType actionType;
	private String symbol;
	private Double volume;
	private Double openPrice;// If openPrice is null, order will be placed at market price
	private Double stopLoss;
	private Double takeProfit;
	private StopUnits stopLossUnits;
	private StopUnits takeProfitUnits;
	private String clientId; // custom trade Id for our use if needed

	public TradeType getActionType() {
		return actionType;
	}

	public void setActionType(TradeType actionType) {
		this.actionType = actionType;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public Double getVolume() {
		return volume;
	}

	public void setVolume(Double volume) {
		this.volume = volume;
	}

	public Double getStopLoss() {
		return stopLoss;
	}

	public void setStopLoss(Double stopLoss) {
		this.stopLoss = stopLoss;
	}

	public Double getTakeProfit() {
		return takeProfit;
	}

	public void setTakeProfit(Double takeProfit) {
		this.takeProfit = takeProfit;
	}

	public StopUnits getStopLossUnits() {
		return stopLossUnits;
	}

	public void setStopLossUnits(StopUnits stopLossUnits) {
		this.stopLossUnits = stopLossUnits;
	}

	public StopUnits getTakeProfitUnits() {
		return takeProfitUnits;
	}

	public void setTakeProfitUnits(StopUnits takeProfitUnits) {
		this.takeProfitUnits = takeProfitUnits;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public Double getOpenPrice() {
		return openPrice;
	}

	public void setOpenPrice(Double openPrice) {
		this.openPrice = openPrice;
	}

	@Override
	public String toString() {
		return "TradeRequest [actionType=" + actionType + ", symbol=" + symbol + ", volume=" + volume + ", openPrice="
				+ openPrice + ", stopLoss=" + stopLoss + ", takeProfit=" + takeProfit + ", stopLossUnits="
				+ stopLossUnits + ", takeProfitUnits=" + takeProfitUnits + ", clientId=" + clientId + "]";
	}
}
