package org.trade.beans.requests;

import org.trade.enums.TradeType;

public class TradeRequest {

	private TradeType actionType;
	private String symbol;
	private Integer volume;
	private Double stopLoss;
	private Double takeProfit;
	private Double stopLossUnits;
	private Integer takeProfitUnits;
	private String clientId; // tradeId

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

	public Integer getVolume() {
		return volume;
	}

	public void setVolume(Integer volume) {
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

	public Double getStopLossUnits() {
		return stopLossUnits;
	}

	public void setStopLossUnits(Double stopLossUnits) {
		this.stopLossUnits = stopLossUnits;
	}

	public Integer getTakeProfitUnits() {
		return takeProfitUnits;
	}

	public void setTakeProfitUnits(Integer takeProfitUnits) {
		this.takeProfitUnits = takeProfitUnits;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	@Override
	public String toString() {
		return "TradeRequest [actionType=" + actionType + ", symbol=" + symbol + ", volume=" + volume + ", stopLoss="
				+ stopLoss + ", takeProfit=" + takeProfit + ", stopLossUnits=" + stopLossUnits + ", takeProfitUnits="
				+ takeProfitUnits + ", clientId=" + clientId + "]";
	}
}
