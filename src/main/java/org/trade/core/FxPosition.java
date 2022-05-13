package org.trade.core;

import java.util.Objects;

import org.ta4j.core.Position;
import org.ta4j.core.Trade;
import org.ta4j.core.Trade.TradeType;
import org.ta4j.core.cost.CostModel;
import org.ta4j.core.num.Num;
import org.trade.utils.meta_api.TradeUtil;
import org.trade.utils.meta_api.beans.TradeRequest;

import cloud.metaapi.sdk.clients.meta_api.models.MetatraderTradeResponse;

public class FxPosition extends Position {

	private static final long serialVersionUID = -916068891251660413L;

	private MetatraderTradeResponse order;
	private FxTrade entry;
	private FxTrade exit;
	private TradeType startingType;
	private CostModel transactionCostModel;
	private CostModel holdingCostModel;

	public MetatraderTradeResponse getOrder() {
		return order;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public Trade getEntry() {
		return entry;
	}

	public Trade getExit() {
		return exit;
	}

	public TradeType getStartingType() {
		return startingType;
	}

	public CostModel getTransactionCostModel() {
		return transactionCostModel;
	}

	public CostModel getHoldingCostModel() {
		return holdingCostModel;
	}

	/**
	 * Constructor.
	 * 
	 * @param startingType         the starting {@link TradeType trade type} of the
	 *                             position (i.e. type of the entry trade)
	 * @param transactionCostModel the cost model for transactions of the asset
	 * @param holdingCostModel     the cost model for holding asset (e.g. borrowing)
	 */
	public FxPosition(TradeType startingType, CostModel transactionCostModel, CostModel holdingCostModel) {
		super(startingType, transactionCostModel, holdingCostModel);
		if (startingType == null) {
			throw new IllegalArgumentException("Starting type must not be null");
		}
		this.startingType = startingType;
		this.transactionCostModel = transactionCostModel;
		this.holdingCostModel = holdingCostModel;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Position) {
			Position p = (Position) obj;
			return (entry == null ? p.getEntry() == null : entry.equals(p.getEntry()))
					&& (exit == null ? p.getExit() == null : exit.equals(p.getExit()));
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(entry, exit);
	}

	/**
	 * Operates the position at the index-th position
	 * 
	 * @param index  the bar index
	 * @param price  the price
	 * @param amount the amount
	 * @param symbol
	 * @return the trade
	 */
	public FxTrade operate(int index, Num price, Num amount, String symbol) {
		FxTrade trade = null;
		if (isNew()) {
			trade = new FxTrade(index, startingType, price, amount, transactionCostModel);
			if (executeTrade(startingType, price, amount, symbol))
				entry = trade;
		} else if (isOpened()) {
			if (index < entry.getIndex()) {
				throw new IllegalStateException("The index i is less than the entryTrade index");
			}
			trade = new FxTrade(index, startingType.complementType(), price, amount, transactionCostModel);
			if (executeTrade(startingType.complementType(), price, amount, symbol))
				exit = trade;
		}
		return trade;
	}

	private boolean executeTrade(TradeType tradeType, Num price, Num amount, String symbol) {
		TradeRequest request = new TradeRequest();
		request.setOpenPrice(price.doubleValue());
		request.setVolume(amount.doubleValue());
		request.setSymbol(symbol);
		if (tradeType.equals(TradeType.BUY)) {
			order = TradeUtil.createLimitBuyOrder(request);
		} else {
			order = TradeUtil.createLimitSellOrder(request);
		}
		return !(order == null);
	}

	/**
	 * @return true if the position is closed, false otherwise
	 */
	public boolean isClosed() {
		return (entry != null) && (exit != null);
	}

	/**
	 * @return true if the position is opened, false otherwise
	 */
	public boolean isOpened() {
		return (entry != null) && (exit == null);
	}

	/**
	 * @return true if the position is new, false otherwise
	 */
	public boolean isNew() {
		return (entry == null) && (exit == null);
	}

	public boolean isPending() {
		return isOpened() && order != null && order.positionId == null;
	}

	@Override
	public String toString() {
		return "Entry: " + entry + " exit: " + exit;
	}

}
