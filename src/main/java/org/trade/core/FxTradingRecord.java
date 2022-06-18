package org.trade.core;

import java.util.ArrayList;
import java.util.List;

import org.jfree.util.Log;
import org.ta4j.core.Position;
import org.ta4j.core.Trade;
import org.ta4j.core.Trade.TradeType;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.cost.CostModel;
import org.ta4j.core.cost.ZeroCostModel;
import org.ta4j.core.num.Num;
import org.trade.utils.TelegramUtils;

public class FxTradingRecord implements TradingRecord {

	private static final long serialVersionUID = 2134401109482839832L;

	/**
	 * The name of the trading record
	 */
	private String name;

	/**
	 * The recorded trades
	 */
	private List<Trade> trades = new ArrayList<>();

	/**
	 * The recorded BUY trades
	 */
	private List<Trade> buyTrades = new ArrayList<>();

	/**
	 * The recorded SELL trades
	 */
	private List<Trade> sellTrades = new ArrayList<>();

	/**
	 * The recorded entry trades
	 */
	private List<Trade> entryTrades = new ArrayList<>();

	/**
	 * The recorded exit trades
	 */
	private List<Trade> exitTrades = new ArrayList<>();

	/**
	 * The entry type (BUY or SELL) in the trading session
	 */
	private TradeType startingType;

	/**
	 * The recorded positions
	 */
	private List<Position> positions = new ArrayList<>();

	/**
	 * The current non-closed position (there's always one)
	 */
	private FxPosition currentPosition;

	/**
	 * Trading cost models
	 */
	private CostModel transactionCostModel;
	private CostModel holdingCostModel;

	/**
	 * Constructor.
	 */
	public FxTradingRecord() {
		this(TradeType.BUY);
	}

	/**
	 * Constructor.
	 *
	 * @param name the name of the tradingRecord
	 */
	public FxTradingRecord(String name) {
		this(TradeType.BUY);
		this.name = name;
	}

	/**
	 * Constructor.
	 *
	 * @param name           the name of the trading record
	 * @param entryTradeType the {@link TradeType trade type} of entries in the
	 *                       trading session
	 */
	public FxTradingRecord(String name, TradeType tradeType) {
		this(tradeType, new ZeroCostModel(), new ZeroCostModel());
		this.name = name;
	}

	/**
	 * Constructor.
	 *
	 * @param entryTradeType the {@link TradeType trade type} of entries in the
	 *                       trading session
	 */
	public FxTradingRecord(TradeType tradeType) {
		this(tradeType, new ZeroCostModel(), new ZeroCostModel());
	}

	/**
	 * Constructor.
	 *
	 * @param entryTradeType       the {@link TradeType trade type} of entries in
	 *                             the trading session
	 * @param transactionCostModel the cost model for transactions of the asset
	 * @param holdingCostModel     the cost model for holding asset (e.g. borrowing)
	 */
	public FxTradingRecord(TradeType entryTradeType, CostModel transactionCostModel, CostModel holdingCostModel) {
		if (entryTradeType == null) {
			throw new IllegalArgumentException("Starting type must not be null");
		}
		this.startingType = entryTradeType;
		this.transactionCostModel = transactionCostModel;
		this.holdingCostModel = holdingCostModel;
		currentPosition = new FxPosition(entryTradeType, transactionCostModel, holdingCostModel);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public TradeType getStartingType() {
		return startingType;
	}

	@Override
	public FxPosition getCurrentPosition() {
		return currentPosition;
	}

	public void operate(int index, Num price, Num amount, Double stopLoss, Double takeProfit) {
		if (currentPosition.isClosed()) {
			// Current position closed, should not occur
			throw new IllegalStateException("Current position should not be closed");
		}
		boolean newTradeWillBeAnEntry = currentPosition.isNew();
		Trade newTrade = currentPosition.operate(index, price, amount, name, stopLoss, takeProfit);
		recordTrade(newTrade, newTradeWillBeAnEntry);
	}

	public boolean enter(int index, Num price, Num amount, Double stopLoss, Double takeProfit) {
		if (currentPosition.isNew()) {
			operate(index, price, amount, stopLoss, takeProfit);
			return true;
		}
		return false;
	}

	@Override
	public void operate(int index, Num price, Num amount) {
		operate(index, price, amount, null, null);
	}

	@Override
	public boolean enter(int index, Num price, Num amount) {
		return enter(index, price, amount, null, null);
	}

	public void forcedExit(int index, Num price, Num amount) {
		Trade trade = currentPosition.forcedExit(index, price, amount);
		recordTrade(trade, false);
	}

	@Override
	public boolean exit(int index, Num price, Num amount) {
		try {
			if (currentPosition.isOpened() && !currentPosition.isPending()) {
				operate(index, price, amount);
				return true;
			}
		} catch (Exception e) {
			Log.error("Failed to exit trade!", e);
			TelegramUtils.sendMessage("Failed to exit trade\nStrategy: " + Thread.currentThread().getName());
		}

		return false;
	}

	@Override
	public List<Position> getPositions() {
		return positions;
	}

	@Override
	public Trade getLastTrade() {
		if (!trades.isEmpty()) {
			return trades.get(trades.size() - 1);
		}
		return null;
	}

	@Override
	public Trade getLastTrade(TradeType tradeType) {
		if (TradeType.BUY.equals(tradeType) && !buyTrades.isEmpty()) {
			return buyTrades.get(buyTrades.size() - 1);
		} else if (TradeType.SELL.equals(tradeType) && !sellTrades.isEmpty()) {
			return sellTrades.get(sellTrades.size() - 1);
		}
		return null;
	}

	@Override
	public Trade getLastEntry() {
		if (!entryTrades.isEmpty()) {
			return entryTrades.get(entryTrades.size() - 1);
		}
		return null;
	}

	@Override
	public Trade getLastExit() {
		if (!exitTrades.isEmpty()) {
			return exitTrades.get(exitTrades.size() - 1);
		}
		return null;
	}

	/**
	 * Records an trade and the corresponding position (if closed).
	 *
	 * @param trade   the trade to be recorded
	 * @param isEntry true if the trade is an entry, false otherwise (exit)
	 */
	private void recordTrade(Trade trade, boolean isEntry) {
		if (trade == null) {
			throw new IllegalArgumentException("Trade should not be null");
		}

		// Storing the new trade in entries/exits lists
		if (isEntry) {
			entryTrades.add(trade);
		} else {
			exitTrades.add(trade);
		}

		// Storing the new trade in trades list
		trades.add(trade);
		if (TradeType.BUY.equals(trade.getType())) {
			// Storing the new trade in buy trades list
			buyTrades.add(trade);
		} else if (TradeType.SELL.equals(trade.getType())) {
			// Storing the new trade in sell trades list
			sellTrades.add(trade);
		}

		// Storing the position if closed
		if (currentPosition.isClosed()) {
			positions.add(currentPosition);
			currentPosition = new FxPosition(startingType, transactionCostModel, holdingCostModel);
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("FxTradingRecord: " + name != null ? name : "" + "\n");
		for (Trade trade : trades) {
			sb.append(trade.toString()).append("\n");
		}
		return sb.toString();
	}
}
